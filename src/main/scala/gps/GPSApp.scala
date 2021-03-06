package gps

import chisel3._

/**
  * Make an unapply function for the argument parser.
  * It allows us to match on parameters that are integers
  */
object Int {
  def unapply(v: String): Option[Int] = {
    try {
      Some(v.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }
}


/**
 * Define entry point for CORDIC generator
 */
object GPSApp extends App {
  val usage = s"""GPS arguments:
  |--sampleWidth <Int>\t\tADC sample width
  |--dataWidth <Int>\t\tWidth of input data
  |""".stripMargin
  /**
   * Parse arguments
   *
   * Some arguments are used by the cordic generator and are used to construct a FixedCordicParams object.
   * The rest get returned as a List[String] to pass to the Chisel driver
   *
   */
  def argParse(args: List[String], params: GPSParams): (List[String], GPSParams) = {
    args match {
      case "--help" :: tail =>
        println(usage)
        val (newArgs, newParams) = argParse(tail, params)
        ("--help" +: newArgs, newParams)
      case "--sampleWidth" :: Int(sampleW) :: tail => argParse(tail, params.copy(sampleWidth = sampleW))
      case "--dataWidth" :: Int(dataW) :: tail => argParse(tail, params.copy(dataWidth = dataW))
      case chiselOpt :: tail => {
        val (newArgs, newParams) = argParse(tail, params)
        (chiselOpt +: newArgs, newParams)
      }
      case Nil => (args, params)
    }
  }
  val defaultParams = GPSParams(
    sampleWidth = 5,
    dataWidth = 32
  )
  val (chiselArgs, params) = argParse(args.toList, defaultParams)
  // Run the Chisel driver to generate a cordic
  Driver.execute(chiselArgs.toArray, () => new GPS(params))
}
