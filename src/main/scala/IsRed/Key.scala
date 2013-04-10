package com.roundeights.isred

import scala.language.implicitConversions

/**
 * Implicit conversions
 */
object Key {

    /** String to key */
    implicit def string2key ( key: String ) = new Key( key )
}

/**
 * A redis key
 */
case class Key ( override val toString: String )

