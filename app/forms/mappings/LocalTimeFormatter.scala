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
import models.RichString
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalTime
import scala.util.{Failure, Success, Try}

private[mappings] class LocalTimeFormatter(
  override val invalidKey: String,
  override val requiredKey: String
) extends Formatter[LocalTime]
    with LocalDateTimeFormatter {

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalTime] = {
    def bind(field: Field): Either[Seq[FormError], String] =
      stringFormatter(requiredKey, Seq(field.key))(_.removeSpaces()).bind(field.id(key), data)

    def bindHour: Either[FieldError, Int] =
      bind(HourField) match {
        case Left(_) =>
          Left(FieldError(HourField, requiredError))
        case Right(value) =>
          Try(Integer.parseInt(value)) match {
            case Success(hour) if 0 to 23 contains hour =>
              Right(hour)
            case _ =>
              Left(FieldError(HourField, invalidError))
          }
      }

    def bindMinute: Either[FieldError, Int] =
      bind(MinuteField) match {
        case Left(_) =>
          Left(FieldError(MinuteField, requiredError))
        case Right(value) =>
          Try(Integer.parseInt(value)) match {
            case Success(minute) if 0 to 59 contains minute =>
              Right(minute)
            case _ =>
              Left(FieldError(MinuteField, invalidError))
          }
      }

    def toTime(hour: Int, minute: Int): Either[Seq[FormError], LocalTime] =
      Try(LocalTime.of(hour, minute, 0)) match {
        case Success(time) =>
          Right(time)
        case Failure(_) =>
          Left(
            Seq(
              FieldError(HourField, invalidError).toFormError(key),
              FieldError(MinuteField, invalidError).toFormError(key)
            )
          )
      }

    (bindHour, bindMinute) match {
      case (Right(hour), Right(minute)) =>
        toTime(hour, minute)
      case (hourBinding, minuteBinding) =>
        Left(Seq(hourBinding, minuteBinding).toFormErrors(key))
    }
  }

  override def unbind(key: String, value: LocalTime): Map[String, String] =
    Map(
      HourField.id(key)   -> value.getHour.toString,
      MinuteField.id(key) -> value.getMinute.toString
    )
}

object LocalTimeFormatter {

  val fieldKeys: List[String] = List(HourField.key, MinuteField.key)
}
