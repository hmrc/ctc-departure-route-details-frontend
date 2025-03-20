/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.mappings

import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

import java.time.LocalTime

class TimeMappingsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues with Mappings {

  val form: Form[LocalTime] = Form(
    "value" -> localTime(
      invalidKey = "error.invalid",
      requiredKey = "error.required"
    )
  )

  val invalidField: Gen[String] = nonEmptyString.retryUntil(_.toIntOption.isEmpty)

  val genTime: Gen[LocalTime] = arbitraryLocalTime.arbitrary

  "must bind valid data" in {

    forAll(genTime -> "valid time") {
      time =>
        val data = Map(
          "valueHour"   -> time.getHour.toString,
          "valueMinute" -> time.getMinute.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual time
    }
  }

  "must bind valid data with spaces" in {

    forAll(genTime -> "valid time") {
      time =>
        val data = Map(
          "valueHour"   -> s"${time.getHour.toString}   ",
          "valueMinute" -> s"${time.getMinute.toString}   "
        )

        val result = form.bind(data)

        result.value.value mustEqual time
    }
  }

  "must fail to bind an empty time" in {

    val result = form.bind(Map.empty[String, String])

    result.errors must contain only FormError("value", "error.required.multiple", List("hour", "minute"))
  }

  "must fail to bind a time with a missing minute" in {

    forAll(genTime -> "valid time") {
      time =>
        val data = Map(
          "valueHour"   -> time.getHour.toString,
          "valueMinute" -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.minute", List("minute"))
    }
  }

  "must fail to bind a time with an invalid minute" in {

    forAll(genTime -> "valid time", invalidField -> "invalid field") {
      (time, field) =>
        val data = Map(
          "valueHour"   -> time.getHour.toString,
          "valueMinute" -> field
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.minute", List("minute"))
    }
  }

  "must fail to bind a time with a missing hour" in {

    forAll(genTime -> "valid time") {
      time =>
        val data = Map(
          "valueHour"   -> "",
          "valueMinute" -> time.getMinute.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.hour", List("hour"))
    }
  }

  "must fail to bind a time with an invalid hour" in {

    forAll(genTime -> "valid data", invalidField -> "invalid field") {
      (time, field) =>
        val data = Map(
          "valueHour"   -> field,
          "valueMinute" -> time.getMinute.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.hour", List("hour"))
    }
  }

  "must fail to bind a time with a missing hour and an invalid minute" in {

    forAll(invalidField -> "invalid field") {
      field =>
        val data = Map(
          "valueHour"   -> "",
          "valueMinute" -> field
        )

        val result = form.bind(data)

        result.errors mustEqual Seq(
          FormError("value", "error.required.hour", List("hour")),
          FormError("value", "error.invalid.minute", List("minute"))
        )
    }
  }

  "must fail to bind a time with an invalid hour and a missing minute" in {

    forAll(invalidField -> "invalid field") {
      field =>
        val data = Map(
          "valueHour"   -> field,
          "valueMinute" -> ""
        )

        val result = form.bind(data)

        result.errors mustEqual Seq(
          FormError("value", "error.invalid.hour", List("hour")),
          FormError("value", "error.required.minute", List("minute"))
        )
    }
  }

  "must fail to bind an invalid time" - {

    "when time contains an hour not between 0 and 23" in {

      val data = Map(
        "valueHour"   -> "24",
        "valueMinute" -> "13"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.hour", List("hour"))
      )
    }

    "when time contains a minute not between 0 and 59" in {

      val data = Map(
        "valueHour"   -> "12",
        "valueMinute" -> "60"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.minute", List("minute"))
      )
    }

    "when hour and minute are both invalid numbers" in {

      val data = Map(
        "valueHour"   -> "24",
        "valueMinute" -> "60"
      )

      val result = form.bind(data)

      result.errors must contain only FormError("value", "error.invalid.multiple", List("hour", "minute"))
    }

    "when hour and minute are invalid characters" in {

      forAll(invalidField -> "invalid minute", invalidField -> "invalid hour") {
        (minute, hour) =>
          val data = Map(
            "valueHour"   -> hour,
            "valueMinute" -> minute
          )

          val result = form.bind(data)

          result.errors must contain only FormError("value", "error.invalid.multiple", List("hour", "minute"))
      }
    }
  }

  "must unbind a time" in {

    forAll(genTime -> "valid time") {
      time =>
        val filledForm = form.fill(time)

        filledForm("valueHour").value.value mustEqual time.getHour.toString
        filledForm("valueMinute").value.value mustEqual time.getMinute.toString
    }
  }
}
