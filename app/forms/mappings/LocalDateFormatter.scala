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

  private val fieldKeys: List[String] = List("Day", "Month", "Year")

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, s"$invalidKey.all", args)))
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
    fieldKeys.foldLeft[Seq[Either[FormError, Int]]](Nil) {
      (acc, fieldKey) =>
        intFormatter(requiredKey, invalidKey, invalidKey, fieldKey.toLowerCase +: args).bind(s"$key$fieldKey", data) match {
          case Left(formErrors) => acc ++ formErrors.map(Left(_))
          case Right(value)     => acc :+ Right(value)
        }
    } match {
      case Seq(Right(day), Right(month), Right(year)) =>
        toDate(key, day, month, year)
      case errors =>
        def collectErrors(errorKey: String): Seq[FormError] =
          errors.collect {
            case Left(FormError(_, `errorKey` :: _, args)) => args
          }.flatten match {
            case args if args.size == 3 => Seq(FormError(key, s"$errorKey.all"))
            case args if args.size == 2 => Seq(FormError(key, s"$errorKey.multiple", args))
            case args if args.size == 1 => Seq(FormError(key, errorKey, args))
            case _                      => Nil
          }

        Left(collectErrors(requiredKey) ++ collectErrors(invalidKey))
    }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"${key}Day"   -> value.getDayOfMonth.toString,
      s"${key}Month" -> value.getMonthValue.toString,
      s"${key}Year"  -> value.getYear.toString
    )
}
