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

package forms

import forms.behaviours.FieldBehaviours
import generators.Generators
import models.DateTime
import org.scalacheck.Gen
import play.api.data.{Form, FormError}
import utils.Format.RichLocalDate

class DateTimeFormProviderSpec extends FieldBehaviours with Generators {

  private val prefix = Gen.alphaNumStr.sample.value

  private val maxDate = s"$prefix.date.error.futureDate"
  private val minDate = s"$prefix.date.error.pastDate"

  private val fieldName = "date"

  "dateTime" - {

    "must bind valid data" in {

      val localDateTime = arbitraryLocalDateTime.arbitrary

      forAll(localDateTime) {
        dateTime =>
          val data: Map[String, String] = Map(
            "timeHour"   -> dateTime.getHour.toString,
            "timeMinute" -> dateTime.getMinute.toString,
            "dateDay"    -> dateTime.getDayOfMonth.toString,
            "dateMonth"  -> dateTime.getMonthValue.toString,
            "dateYear"   -> dateTime.getYear.toString
          )

          val dateBefore = dateTime.toLocalDate.minusDays(1)
          val dateAfter  = dateTime.toLocalDate.plusDays(1)

          val form = new DateTimeFormProvider()(prefix, dateBefore, dateAfter)

          val result: Form[DateTime] = form.bind(data)

          val date = dateTime.toLocalDate
          val time = dateTime.toLocalTime

          result.errors mustBe List.empty
          result.value.value mustBe DateTime(date, time)
      }
    }

    "must not bind when date is above max date" in {

      val localDateTime = arbitraryLocalDateTime.arbitrary

      forAll(localDateTime) {
        dateTime =>
          val invalidDateTime = dateTime.plusDays(2)

          val data: Map[String, String] = Map(
            "timeHour"   -> invalidDateTime.getHour.toString,
            "timeMinute" -> invalidDateTime.getMinute.toString,
            "dateDay"    -> invalidDateTime.getDayOfMonth.toString,
            "dateMonth"  -> invalidDateTime.getMonthValue.toString,
            "dateYear"   -> invalidDateTime.getYear.toString
          )

          val dateBefore = dateTime.toLocalDate.minusDays(1)
          val dateAfter  = dateTime.toLocalDate.plusDays(1)

          val form = new DateTimeFormProvider()(prefix, dateBefore, dateAfter)

          val result: Form[DateTime] = form.bind(data)

          val formattedArg = invalidDateTime.toLocalDate.formatAsString

          result.errors mustEqual Seq(FormError(fieldName, List(maxDate), List(formattedArg, "day", "month", "year")))
      }
    }

    "must not bind when date is below min date" in {

      val localDateTime = arbitraryLocalDateTime.arbitrary

      forAll(localDateTime) {
        dateTime =>
          val invalidDateTime = dateTime.minusDays(2)

          val data: Map[String, String] = Map(
            "timeHour"   -> invalidDateTime.getHour.toString,
            "timeMinute" -> invalidDateTime.getMinute.toString,
            "dateDay"    -> invalidDateTime.getDayOfMonth.toString,
            "dateMonth"  -> invalidDateTime.getMonthValue.toString,
            "dateYear"   -> invalidDateTime.getYear.toString
          )

          val dateBefore = dateTime.toLocalDate.minusDays(1)
          val dateAfter  = dateTime.toLocalDate.plusDays(1)

          val form = new DateTimeFormProvider()(prefix, dateBefore, dateAfter)

          val result: Form[DateTime] = form.bind(data)

          val formattedArg = invalidDateTime.toLocalDate.formatAsString

          result.errors mustEqual Seq(FormError(fieldName, List(minDate), List(formattedArg, "day", "month", "year")))
      }
    }
  }
}
