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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.Constants.unloadingLocationMaxLength
import forms.behaviours.StringFieldBehaviours
import models.domain.StringFieldRegex.stringFieldRegex
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class UnloadingLocationFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val prefix = Gen.alphaNumStr.sample.value
  val requiredKey    = s"$prefix.error.required"
  val lengthKey      = s"$prefix.error.length"
  val invalidKey     = s"$prefix.error.invalid"

  private val formProvider       = new UnloadingLocationFormProvider()
  private val form: Form[String] = formProvider(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(unloadingLocationMaxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = unloadingLocationMaxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(unloadingLocationMaxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(stringFieldRegex.regex)),
      unloadingLocationMaxLength
    )
  }
}
