package com.roundeights.isred

import scala.language.implicitConversions

/**
 * Implicit conversions
 */
object Key {

    /** String to key */
    implicit def string2key ( key: String ) = new Key( key )

    /** String to key */
    implicit def tuple2key ( tuple: (String, String) )
        = ( new Key( tuple._1 ) -> tuple._2 )
}

/**
 * A redis key
 */
case class Key ( override val toString: String )

