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

import forms.mappings.LocalDateFormatter.{dayField, fieldKeys, monthField, yearField}
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  invalidKey: String,
  requiredKey: String,
  args: Seq[String] = Seq.empty
) extends Formatter[LocalDate]
    with Formatters {

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, s"$invalidKey.all", fieldKeys)))
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    def binding(fieldKey: String): Either[Seq[FormError], Int] =
      intFormatter(requiredKey, invalidKey, invalidKey, Seq(fieldKey)).bind(s"$key${fieldKey.capitalize}", data)

    val dayBinding   = binding(dayField)
    val monthBinding = binding(monthField)
    val yearBinding  = binding(yearField)

    (dayBinding, monthBinding, yearBinding) match {
      case (Right(day), Right(month), Right(year)) =>
        toDate(key, day, month, year)
      case _ =>
        Left {
          Seq(dayBinding, monthBinding, yearBinding)
            .collect {
              case Left(formErrors) => formErrors
            }
            .flatten
            .groupByPreserveOrder(_.message)
            .map {
              case (errorKey, formErrors) => errorKey -> formErrors.toSeq.flatMap(_.args)
            }
            .flatMap {
              case (errorKey, args) if args.size == 3 => Seq(FormError(key, s"$errorKey.all", args))
              case (errorKey, args) if args.size == 2 => Seq(FormError(key, s"$errorKey.multiple", args))
              case (errorKey, args) if args.size == 1 => Seq(FormError(key, errorKey, args))
              case _                                  => Nil
            }
        }
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"${key}Day"   -> value.getDayOfMonth.toString,
      s"${key}Month" -> value.getMonthValue.toString,
      s"${key}Year"  -> value.getYear.toString
    )
}

object LocalDateFormatter {
  val dayField: String        = "day"
  val monthField: String      = "month"
  val yearField: String       = "year"
  val fieldKeys: List[String] = List(dayField, monthField, yearField)
}
