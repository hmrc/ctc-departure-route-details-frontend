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

import forms.Constants.exactLoCodeLength
import forms.behaviours.StringFieldBehaviours
import generators.Generators
import models.domain.StringFieldRegex.alphaNumericRegex
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class UnLocodeFormProviderSpec extends StringFieldBehaviours with Generators {

  private val prefix      = Gen.alphaNumStr.sample.value
  private val requiredKey = s"$prefix.error.required"
  private val lengthKey   = s"$prefix.error.length"
  private val invalidKey  = s"$prefix.error.invalid"

  val invalidUnLocodeOverLength: Gen[String] = stringsLongerThan(exactLoCodeLength + 1)

  private val form = new UnLocodeFormProvider()(prefix)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyString
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form = form,
      fieldName = fieldName,
      maxLength = exactLoCodeLength,
      lengthError = FormError(fieldName, lengthKey, Seq(exactLoCodeLength)),
      gen = invalidUnLocodeOverLength
    )

    behave like fieldWithMinLength(
      form = form,
      fieldName = fieldName,
      minLength = exactLoCodeLength,
      lengthError = FormError(fieldName, lengthKey, Seq(exactLoCodeLength))
    )

    behave like fieldWithInvalidCharacters(
      form = form,
      fieldName = fieldName,
      error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex.regex)),
      length = exactLoCodeLength
    )

    "must convert string to uppercase" in {
      val value                = stringsWithExactLength(exactLoCodeLength).sample.value
      val result: Form[String] = form.bind(Map(fieldName -> value))
      result.value.get mustBe value.toUpperCase
      result.errors mustEqual Nil
    }
  }
}
