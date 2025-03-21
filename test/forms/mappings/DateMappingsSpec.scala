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

import java.time.LocalDate

class DateMappingsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues with Mappings {

  val form = Form(
    "value" -> localDate(
      requiredKey = "error.required",
      invalidKey = "error.invalid"
    )
  )

  val validData = datesBetween(
    min = LocalDate.of(2000, 1, 1),
    max = LocalDate.of(3000, 1, 1)
  )

  val invalidField: Gen[String] = nonEmptyString.retryUntil(_.toIntOption.isEmpty)

  "must bind valid data" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
    }
  }

  "must bind valid data after trim" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> s"   ${date.getDayOfMonth.toString}   ",
          "valueMonth" -> s"   ${date.getMonthValue.toString}   ",
          "valueYear"  -> s"   ${date.getYear.toString}   "
        )

        val result = form.bind(data)

        result.value.value mustEqual date
    }
  }

  "must bind valid data after spaces removed" in {

    val data = Map(
      "valueDay"   -> " 1 9 ",
      "valueMonth" -> " 1 2 ",
      "valueYear"  -> " 2 0 2 5 "
    )

    val result = form.bind(data)

    result.value.value mustEqual LocalDate.of(2025, 12, 19)
  }

  "must fail to bind an empty date" in {

    val result = form.bind(Map.empty[String, String])

    result.errors must contain only FormError("value", "error.required.all", List("day", "month", "year"))
  }

  "must fail to bind a date with a missing day" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> "",
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.day", List("day"))
    }
  }

  "must fail to bind a date with an invalid day" in {

    forAll(validData -> "valid date", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "valueDay"   -> field,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        val days = date.getMonth.length(date.isLeapYear)

        result.errors must contain only FormError("value", "error.invalid.day", List(days, "day"))
    }
  }

  "must fail to bind a date with a missing month" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> "",
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.month", List("month"))
    }
  }

  "must fail to bind a date with an invalid month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> field,
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.month", List("month"))
    }
  }

  "must fail to bind a date with a missing day and an invalid month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "valueDay"   -> "",
          "valueMonth" -> field,
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors mustEqual Seq(
          FormError("value", "error.required.day", List("day")),
          FormError("value", "error.invalid.month", List("month"))
        )
    }
  }

  "must fail to bind a date with an invalid day and a missing month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "valueDay"   -> field,
          "valueMonth" -> "",
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors mustEqual Seq(
          FormError("value", "error.invalid.day", List(31, "day")),
          FormError("value", "error.required.month", List("month"))
        )
    }
  }

  "must fail to bind a date with a missing year" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.year", List("year"))
    }
  }

  "must fail to bind a date with an invalid year" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") {
      (date, field) =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> field
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.year", List("year"))
    }
  }

  "must fail to bind a date with a missing day and month" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> "",
          "valueMonth" -> "",
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.multiple", List("day", "month"))
    }
  }

  "must fail to bind a date with a missing day and year" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> "",
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.multiple", List("day", "year"))
    }
  }

  "must fail to bind a date with a missing month and year" in {

    forAll(validData -> "valid date") {
      date =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> "",
          "valueYear"  -> ""
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.required.multiple", List("month", "year"))
    }
  }

  "must fail to bind an invalid day and month" in {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid month") {
      (date, day, month) =>
        val data = Map(
          "valueDay"   -> day,
          "valueMonth" -> month,
          "valueYear"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.multiple", List("day", "month"))
    }
  }

  "must fail to bind an invalid day and year" in {

    forAll(validData -> "valid date", invalidField -> "invalid day", invalidField -> "invalid year") {
      (date, day, year) =>
        val data = Map(
          "valueDay"   -> day,
          "valueMonth" -> date.getMonthValue.toString,
          "valueYear"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.multiple", List("day", "year"))
    }
  }

  "must fail to bind an invalid month and year" in {

    forAll(validData -> "valid date", invalidField -> "invalid month", invalidField -> "invalid year") {
      (date, month, year) =>
        val data = Map(
          "valueDay"   -> date.getDayOfMonth.toString,
          "valueMonth" -> month,
          "valueYear"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.multiple", List("month", "year"))
    }
  }

  "must fail to bind an invalid day, month and year" in {

    forAll(invalidField -> "valid day", invalidField -> "invalid month", invalidField -> "invalid year") {
      (day, month, year) =>
        val data = Map(
          "valueDay"   -> day,
          "valueMonth" -> month,
          "valueYear"  -> year
        )

        val result = form.bind(data)

        result.errors must contain only FormError("value", "error.invalid.all", List("day", "month", "year"))
    }
  }

  "must fail to bind an invalid date" - {

    "when date is 29th Feb in a non-leap year" in {

      val data = Map(
        "valueDay"   -> "29",
        "valueMonth" -> "2",
        "valueYear"  -> "2025"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.day", List(28, "day"))
      )
    }

    "when date is 31st of a month with only 30 days" in {

      val data = Map(
        "valueDay"   -> "31",
        "valueMonth" -> "4",
        "valueYear"  -> "2025"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.day", List(30, "day"))
      )
    }

    "when date is 32nd of a month with only 28 days" in {

      val data = Map(
        "valueDay"   -> "32",
        "valueMonth" -> "02",
        "valueYear"  -> "2025"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.day", List(28, "day"))
      )
    }

    "when date is 31st of a month with only 28 days in an invalid year" in {

      val data = Map(
        "valueDay"   -> "31",
        "valueMonth" -> "02",
        "valueYear"  -> "foo"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.multiple", List("day", "year"))
      )
    }

    "when date contains a month not between 1 and 12" in {

      val data = Map(
        "valueDay"   -> "15",
        "valueMonth" -> "13",
        "valueYear"  -> "2025"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.month", Seq("month"))
      )
    }

    "when date contains an invalid day and month" in {

      val data = Map(
        "valueDay"   -> "32",
        "valueMonth" -> "13",
        "valueYear"  -> "2025"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.multiple", Seq("day", "month"))
      )
    }

    "when date contains an invalid day, month and year" in {

      val data = Map(
        "valueDay"   -> "foo",
        "valueMonth" -> "foo",
        "valueYear"  -> "foo"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.all", Seq("day", "month", "year"))
      )
    }

    "when date contains an invalid year" in {

      val data = Map(
        "valueDay"   -> "12",
        "valueMonth" -> "12",
        "valueYear"  -> "foo"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.year", Seq("year"))
      )
    }

    "when date contains a year that cannot be parsed as an integer" in {

      val data = Map(
        "valueDay"   -> "12",
        "valueMonth" -> "12",
        "valueYear"  -> "99999999999"
      )

      val result = form.bind(data)

      result.errors mustEqual Seq(
        FormError("value", "error.invalid.year", Seq("year"))
      )
    }
  }

  "must unbind a date" in {

    forAll(validData -> "valid date") {
      date =>
        val filledForm = form.fill(date)

        filledForm("valueDay").value.value mustEqual date.getDayOfMonth.toString
        filledForm("valueMonth").value.value mustEqual date.getMonthValue.toString
        filledForm("valueYear").value.value mustEqual date.getYear.toString
    }
  }
}
