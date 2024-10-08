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

import forms.mappings.LocalTimeFormatter.{fieldKeys, hourField, minuteField}
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalTime
import scala.util.{Failure, Success, Try}

private[mappings] class LocalTimeFormatter(
  invalidKey: String,
  requiredKey: String
) extends Formatter[LocalTime]
    with Formatters {

  private def toTime(key: String, hour: Int, minute: Int): Either[Seq[FormError], LocalTime] =
    Try(LocalTime.of(hour, minute, 0)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, s"$invalidKey.all", fieldKeys)))
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalTime] = {
    def binding(fieldKey: String): Either[Seq[FormError], Int] =
      intFormatter(requiredKey, invalidKey, invalidKey, Seq(fieldKey)).bind(s"$key${fieldKey.capitalize}", data)

    val hourBinding   = binding(hourField)
    val minuteBinding = binding(minuteField)

    (hourBinding, minuteBinding) match {
      case (Right(hour), Right(minute)) =>
        toTime(key, hour, minute)
      case _ =>
        Left {
          Seq(hourBinding, minuteBinding)
            .collect {
              case Left(value) => value
            }
            .flatten
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
  }

  override def unbind(key: String, value: LocalTime): Map[String, String] =
    Map(
      s"${key}Hour"   -> value.getHour.toString,
      s"${key}Minute" -> value.getMinute.toString
    )
}

object LocalTimeFormatter {
  val hourField: String       = "hour"
  val minuteField: String     = "minute"
  val fieldKeys: List[String] = List(hourField, minuteField)
}
