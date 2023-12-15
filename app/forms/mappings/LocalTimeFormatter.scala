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

import java.time.LocalTime
import scala.util.{Failure, Success, Try}

private[mappings] class LocalTimeFormatter(
  invalidKey: String,
  requiredKey: String,
  args: Seq[String] = Seq.empty
) extends Formatter[LocalTime]
    with Formatters {

  private val fieldKeys: List[String] = List("hour", "minute")

  private def toTime(key: String, hour: Int, minute: Int): Either[Seq[FormError], LocalTime] =
    Try(LocalTime.of(hour, minute, 0)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, s"$invalidKey.all", fieldKeys)))
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalTime] =
    fieldKeys.foldLeft[Seq[Either[FormError, Int]]](Nil) {
      (acc, fieldKey) =>
        intFormatter(requiredKey, invalidKey, invalidKey, Seq(fieldKey)).bind(s"$key${fieldKey.capitalize}", data) match {
          case Left(formErrors) => acc ++ formErrors.map(Left(_))
          case Right(value)     => acc :+ Right(value)
        }
    } match {
      case Seq(Right(hour), Right(minute)) =>
        toTime(key, hour, minute)
      case errors =>
        Left {
          errors
            .collect {
              case Left(value) => value
            }
            .groupByPreserveOrder(_.message)
            .map {
              case (errorKey, formErrors) => errorKey -> formErrors.toSeq.flatMap(_.args)
            }
            .flatMap {
              case (errorKey, args) if args.size == 2 => Seq(FormError(key, s"$errorKey.all", args))
              case (errorKey, fieldKey :: Nil)        => Seq(FormError(key, s"$errorKey.$fieldKey", Seq(fieldKey)))
              case _                                  => Nil
            }
        }
    }

  override def unbind(key: String, value: LocalTime): Map[String, String] =
    Map(
      s"${key}Hour"   -> value.getHour.toString,
      s"${key}Minute" -> value.getMinute.toString
    )
}
