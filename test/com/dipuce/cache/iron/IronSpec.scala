package com.dipuce.cache.iron

import org.scalatest.{BeforeAndAfterAll, FunSpec}
import org.specs2.matcher.ShouldMatchers

/**
 * Trait that mixes in all the main test traits I'll use elsewhere
 */
trait IronSpec extends FunSpec with ShouldMatchers with BeforeAndAfterAll
