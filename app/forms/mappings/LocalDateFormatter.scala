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

import forms.mappings.LocalDateTimeFormatter.*
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.{LocalDate, Month, Year}
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  override val invalidKey: String,
  override val requiredKey: String
) extends Formatter[LocalDate]
    with LocalDateTimeFormatter {

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    def bindDay: Either[FieldError, Int] = {
      val days = 31
      bind(key, data, DayField, days)(identity)(1 to days contains _)
    }

    def bindMonth: Either[FieldError, Month] =
      bind(key, data, MonthField)(Month.of) {
        _ => true
      }

    def bindYear: Either[FieldError, Year] =
      bind(key, data, YearField)(Year.of) {
        _ => true
      }

    def toDate(day: Int, month: Month, year: Year): Either[Seq[FormError], LocalDate] =
      Try(LocalDate.of(year.getValue, month, day)) match {
        case Success(date) =>
          Right(date)
        case Failure(_) =>
          lazy val days = month.length(year.isLeap)
          Left(Seq(FieldError(DayField, invalidError, days).toFormError(key)))
      }

    (bindDay, bindMonth, bindYear) match {
      case (Right(day), Right(month), Right(year)) =>
        toDate(day, month, year)
      case (dayBinding, monthBinding, yearBinding) =>
        Left(Seq(dayBinding, monthBinding, yearBinding).toFormErrors(key))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      DayField.id(key)   -> value.getDayOfMonth.toString,
      MonthField.id(key) -> value.getMonthValue.toString,
      YearField.id(key)  -> value.getYear.toString
    )
}

object LocalDateFormatter {

  val fieldKeys: List[String] = List(DayField.key, MonthField.key, YearField.key)
}
