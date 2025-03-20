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

import forms.mappings.Error.*
import play.api.data.FormError

trait LocalDateTimeFormatter extends Formatters {

  val invalidKey: String
  val requiredKey: String

  val invalidError: Error  = InvalidError(invalidKey)
  val requiredError: Error = RequiredError(requiredKey)

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
}

object LocalDateTimeFormatter {}
