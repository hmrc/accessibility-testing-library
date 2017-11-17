/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.accessibility
import java.security.MessageDigest

class CachingChecker[T](checker : AccessibilityChecker[T]) extends AccessibilityChecker[T] {

  private lazy val digest = MessageDigest.getInstance("SHA1")
  var cache: Map[String, Seq[T]] = Map.empty

  override def run(pageSource: String): Seq[T] = {
    val hash = hashcodeOf(pageSource)
    cache.getOrElse(hash, {
      val results: Seq[T] = checker.run(pageSource)
      cache = cache + (hash -> results)
      results
    })
  }

  private def hashcodeOf(source: String): String = {
    digest.digest(source.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
}
