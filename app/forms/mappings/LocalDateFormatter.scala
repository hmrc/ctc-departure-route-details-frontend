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

private[mappings] class LocalDateFormatter(
  override val invalidKey: String,
  override val requiredKey: String
) extends Formatter[LocalDate]
    with LocalDateTimeFormatter {

  private def bindDay(key: String, data: Map[String, String], month: Month, isLeap: Boolean): Either[FieldError, Int] =
    bindDay(key, data, month.length(isLeap))

  private def bindDay(key: String, data: Map[String, String], days: Int): Either[FieldError, Int] =
    bind(key, data, DayField, days)(identity)(1 to days contains _)

  private def bindMonth(key: String, data: Map[String, String]): Either[FieldError, Month] =
    bind(key, data, MonthField)(Month.of) {
      _ => true
    }

  private def bindYear(key: String, data: Map[String, String]): Either[FieldError, Year] =
    bind(key, data, YearField)(Year.of) {
      _ => true
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
    (bindMonth(key, data), bindYear(key, data)) match {
      case (Right(month), Right(year)) =>
        bindDay(key, data, month, year.isLeap) match {
          case Right(day) =>
            Right(LocalDate.of(year.getValue, month, day))
          case dayBinding @ Left(_) =>
            Left(Seq(dayBinding).toFormErrors(key))
        }
      case (Right(month), yearBinding @ Left(_)) =>
        val dayBinding = bindDay(key, data, month, true)
        Left(Seq(dayBinding, yearBinding).toFormErrors(key))
      case (monthBinding, yearBinding) =>
        val dayBinding = bindDay(key, data, 31)
        Left(Seq(dayBinding, monthBinding, yearBinding).toFormErrors(key))
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
