/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.mappings.LocalDateTimeFormatter.Field
import models.RichString
import play.api.data.FormError

import scala.util.{Success, Try}

trait LocalDateTimeFormatter extends Formatters {

  val invalidKey: String
  val requiredKey: String

  def bind[T](
    key: String,
    data: Map[String, String],
    field: Field,
    args: Any*
  )(f: Int => T)(predicate: T => Boolean): Either[FieldError, T] =
    stringFormatter(requiredKey, Seq(field.key))(_.removeSpaces()).bind(field.id(key), data) match {
      case Left(errors) =>
        Left(FieldError(field, RequiredError(requiredKey)))
      case Right(value) =>
        Try(f(Integer.parseInt(value))) match {
          case Success(t) if predicate(t) =>
            Right(t)
          case _ =>
            Left(FieldError(field, InvalidError(invalidKey), args*))
        }
    }

  implicit class RichBindings(value: Seq[Either[FieldError, ?]]) {

    def toFormErrors(key: String): Seq[FormError] =
      value
        .collect {
          case Left(fieldErrors) => fieldErrors
        }
        .groupByPreserveOrder(_.error)
        .flatMap {
          case (error, errors) if errors.size == 3 => Seq(FormError(key, s"${error.key}.all", errors.toSeq.map(_.field.key)))
          case (error, errors) if errors.size == 2 => Seq(FormError(key, s"${error.key}.multiple", errors.toSeq.map(_.field.key)))
          case (_, error :: Nil)                   => Seq(FormError(key, error.messageKey, error.args :+ error.field.key))
          case _                                   => Nil
        }
  }

  sealed trait Error {
    val key: String
  }

  private case class RequiredError(key: String) extends Error

  private case class InvalidError(key: String) extends Error

  case class FieldError(field: Field, error: Error, args: Any*) {

    val messageKey: String = s"${error.key}.${field.key}"
  }
}

object LocalDateTimeFormatter {

  sealed trait Field {
    val key: String

    def id(field: String): String = s"$field${key.capitalize}"
  }

  case object MinuteField extends Field {
    override val key: String = "minute"
  }

  case object HourField extends Field {
    override val key: String = "hour"
  }

  case object DayField extends Field {
    override val key: String = "day"
  }

  case object MonthField extends Field {
    override val key: String = "month"
  }

  case object YearField extends Field {
    override val key: String = "year"
  }
}
