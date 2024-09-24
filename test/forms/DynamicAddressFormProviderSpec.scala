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
import forms.behaviours.StringFieldBehaviours
import models.AddressLine.{City, NumberAndStreet, PostalCode}
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.test.Helpers.running

class DynamicAddressFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val prefix = Gen.alphaNumStr.sample.value
  private val arg1   = Gen.alphaNumStr.sample.value
  private val arg2   = Gen.alphaNumStr.sample.value
  private val args   = Seq(arg1, arg2)

  private val requiredKey = s"$prefix.error.required"
  private val lengthKey   = s"$prefix.error.length"
  private val invalidKey  = s"$prefix.error.invalid"

  private val formProvider = new DynamicAddressFormProvider()(phaseConfig)

  "when postal code is required" - {

    val isPostalCodeRequired = true
    val form                 = formProvider.apply(prefix, isPostalCodeRequired, args*)

    ".numberAndStreet" - {

      val fieldName = NumberAndStreet.field

      "during transition" - {
        val app                      = transitionApplicationBuilder().build()
        val maxNumberAndStreetLength = 35

        running(app) {
          val form = app.injector.instanceOf[DynamicAddressFormProvider].apply(prefix, isPostalCodeRequired, args*)

          behave like fieldThatBindsValidData(
            form = form,
            fieldName = fieldName,
            validDataGenerator = stringsWithMaxLength(maxNumberAndStreetLength)
          )

          behave like fieldWithMaxLength(
            form = form,
            fieldName = fieldName,
            maxLength = maxNumberAndStreetLength,
            lengthError = FormError(fieldName, lengthKey, Seq(NumberAndStreet.arg, arg1, arg2, maxNumberAndStreetLength))
          )

          behave like mandatoryField(
            form = form,
            fieldName = fieldName,
            requiredError = FormError(fieldName, requiredKey, Seq(NumberAndStreet.arg, arg1, arg2))
          )

          behave like fieldWithInvalidCharacters(
            form = form,
            fieldName = fieldName,
            error = FormError(fieldName, invalidKey, Seq(NumberAndStreet.arg, arg1, arg2)),
            length = maxNumberAndStreetLength
          )
        }
      }

      "post transition" - {
        val app                      = postTransitionApplicationBuilder().build()
        val maxNumberAndStreetLength = 70

        running(app) {
          val form = app.injector.instanceOf[DynamicAddressFormProvider].apply(prefix, isPostalCodeRequired, args*)

          behave like fieldThatBindsValidData(
            form = form,
            fieldName = fieldName,
            validDataGenerator = stringsWithMaxLength(maxNumberAndStreetLength)
          )

          behave like fieldWithMaxLength(
            form = form,
            fieldName = fieldName,
            maxLength = maxNumberAndStreetLength,
            lengthError = FormError(fieldName, lengthKey, Seq(NumberAndStreet.arg, arg1, arg2, maxNumberAndStreetLength))
          )

          behave like mandatoryField(
            form = form,
            fieldName = fieldName,
            requiredError = FormError(fieldName, requiredKey, Seq(NumberAndStreet.arg, arg1, arg2))
          )

          behave like fieldWithInvalidCharacters(
            form = form,
            fieldName = fieldName,
            error = FormError(fieldName, invalidKey, Seq(NumberAndStreet.arg, arg1, arg2)),
            length = maxNumberAndStreetLength
          )
        }
      }

      ".city" - {

        val fieldName = City.field

        behave like fieldThatBindsValidData(
          form = form,
          fieldName = fieldName,
          validDataGenerator = stringsWithMaxLength(City.length)
        )

        behave like fieldWithMaxLength(
          form = form,
          fieldName = fieldName,
          maxLength = City.length,
          lengthError = FormError(fieldName, lengthKey, Seq(City.arg, arg1, arg2, City.length))
        )

        behave like mandatoryField(
          form = form,
          fieldName = fieldName,
          requiredError = FormError(fieldName, requiredKey, Seq(City.arg, arg1, arg2))
        )

        behave like fieldWithInvalidCharacters(
          form = form,
          fieldName = fieldName,
          error = FormError(fieldName, invalidKey, Seq(City.arg, arg1, arg2)),
          length = City.length
        )
      }

      ".postalCode" - {

        val postcodeInvalidKey = s"$prefix.error.postalCode.invalid"

        val fieldName = PostalCode.field

        val invalidPostalOverLength = stringsLongerThan(PostalCode.length + 1)

        behave like fieldThatBindsValidData(
          form = form,
          fieldName = fieldName,
          validDataGenerator = stringsWithMaxLength(PostalCode.length)
        )

        behave like fieldWithMaxLength(
          form = form,
          fieldName = fieldName,
          maxLength = PostalCode.length,
          lengthError = FormError(fieldName, lengthKey, Seq(PostalCode.arg, arg1, arg2, PostalCode.length)),
          gen = invalidPostalOverLength
        )

        behave like mandatoryField(
          form = form,
          fieldName = fieldName,
          requiredError = FormError(fieldName, requiredKey, Seq(PostalCode.arg, arg1, arg2))
        )

        behave like fieldWithInvalidCharacters(
          form = form,
          fieldName = fieldName,
          error = FormError(fieldName, postcodeInvalidKey, Seq(arg1, arg2)),
          length = PostalCode.length
        )
      }
    }

    "when postal code is not required" - {

      val isPostalCodeRequired = false
      val form                 = formProvider.apply(prefix, isPostalCodeRequired, args*)

      ".numberAndStreet" - {

        val fieldName = NumberAndStreet.field

        "during transition" - {
          val app                      = transitionApplicationBuilder().build()
          val maxNumberAndStreetLength = 35

          running(app) {
            val form = app.injector.instanceOf[DynamicAddressFormProvider].apply(prefix, isPostalCodeRequired, args*)

            behave like fieldThatBindsValidData(
              form = form,
              fieldName = fieldName,
              validDataGenerator = stringsWithMaxLength(maxNumberAndStreetLength)
            )

            behave like fieldWithMaxLength(
              form = form,
              fieldName = fieldName,
              maxLength = maxNumberAndStreetLength,
              lengthError = FormError(fieldName, lengthKey, Seq(NumberAndStreet.arg, arg1, arg2, maxNumberAndStreetLength))
            )

            behave like mandatoryField(
              form = form,
              fieldName = fieldName,
              requiredError = FormError(fieldName, requiredKey, Seq(NumberAndStreet.arg, arg1, arg2))
            )

            behave like fieldWithInvalidCharacters(
              form = form,
              fieldName = fieldName,
              error = FormError(fieldName, invalidKey, Seq(NumberAndStreet.arg, arg1, arg2)),
              length = maxNumberAndStreetLength
            )
          }
        }

        "post transition" - {
          val app                      = postTransitionApplicationBuilder().build()
          val maxNumberAndStreetLength = 70

          running(app) {
            val form = app.injector.instanceOf[DynamicAddressFormProvider].apply(prefix, isPostalCodeRequired, args*)

            behave like fieldThatBindsValidData(
              form = form,
              fieldName = fieldName,
              validDataGenerator = stringsWithMaxLength(maxNumberAndStreetLength)
            )

            behave like fieldWithMaxLength(
              form = form,
              fieldName = fieldName,
              maxLength = maxNumberAndStreetLength,
              lengthError = FormError(fieldName, lengthKey, Seq(NumberAndStreet.arg, arg1, arg2, maxNumberAndStreetLength))
            )

            behave like mandatoryField(
              form = form,
              fieldName = fieldName,
              requiredError = FormError(fieldName, requiredKey, Seq(NumberAndStreet.arg, arg1, arg2))
            )

            behave like fieldWithInvalidCharacters(
              form = form,
              fieldName = fieldName,
              error = FormError(fieldName, invalidKey, Seq(NumberAndStreet.arg, arg1, arg2)),
              length = maxNumberAndStreetLength
            )
          }
        }
      }

      ".city" - {

        val fieldName = City.field

        behave like fieldThatBindsValidData(
          form = form,
          fieldName = fieldName,
          validDataGenerator = stringsWithMaxLength(City.length)
        )

        behave like fieldWithMaxLength(
          form = form,
          fieldName = fieldName,
          maxLength = City.length,
          lengthError = FormError(fieldName, lengthKey, Seq(City.arg, arg1, arg2, City.length))
        )

        behave like mandatoryField(
          form = form,
          fieldName = fieldName,
          requiredError = FormError(fieldName, requiredKey, Seq(City.arg, arg1, arg2))
        )

        behave like fieldWithInvalidCharacters(
          form = form,
          fieldName = fieldName,
          error = FormError(fieldName, invalidKey, Seq(City.arg, arg1, arg2)),
          length = City.length
        )
      }

      ".postalCode" - {

        val postcodeInvalidKey = s"$prefix.error.postalCode.invalid"

        val fieldName = PostalCode.field

        val invalidPostalOverLength = stringsLongerThan(PostalCode.length + 1)

        behave like fieldThatBindsValidData(
          form = form,
          fieldName = fieldName,
          validDataGenerator = stringsWithMaxLength(PostalCode.length)
        )

        behave like fieldWithMaxLength(
          form = form,
          fieldName = fieldName,
          maxLength = PostalCode.length,
          lengthError = FormError(fieldName, lengthKey, Seq(PostalCode.arg, arg1, arg2, PostalCode.length)),
          gen = invalidPostalOverLength
        )

        behave like optionalField(
          form = form,
          fieldName = fieldName
        )

        behave like fieldWithInvalidCharacters(
          form = form,
          fieldName = fieldName,
          error = FormError(fieldName, postcodeInvalidKey, Seq(arg1, arg2)),
          length = PostalCode.length
        )
      }
    }
  }
}
