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

package pages.locationOfGoods

import config.Constants.LocationOfGoodsIdentifier.CustomsOfficeIdentifier
import models.LocationOfGoodsIdentification
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.locationOfGoods.{LocationOfGoodsContactSection, LocationOfGoodsIdentifierSection}
import play.api.libs.json.Json

class IdentificationPageSpec extends PageBehaviours {

  "IdentificationPage" - {

    beRetrievable[LocationOfGoodsIdentification](IdentificationPage)

    beSettable[LocationOfGoodsIdentification](IdentificationPage)

    beRemovable[LocationOfGoodsIdentification](IdentificationPage)

    "cleanup" - {
      "must clean up LocationOfGoodsIdentifierSection and inferred value" in {
        forAll(arbitrary[LocationOfGoodsIdentification].retryUntil(_.code != CustomsOfficeIdentifier)) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(InferredIdentificationPage, qualifierOfIdentification)
              .setValue(LocationOfGoodsIdentifierSection, Json.obj("foo" -> "bar"))
              .setValue(AddContactYesNoPage, true)
              .setValue(LocationOfGoodsContactSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification)

            result.get(IdentificationPage) mustBe defined
            result.get(InferredIdentificationPage) must not be defined
            result.get(LocationOfGoodsIdentifierSection) must not be defined
            result.get(AddContactYesNoPage) mustBe defined
            result.get(LocationOfGoodsContactSection) mustBe defined
        }
      }

      "must clean up contact when customs office identifier chosen" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(InferredIdentificationPage, qualifierOfIdentification)
              .setValue(LocationOfGoodsIdentifierSection, Json.obj("foo" -> "bar"))
              .setValue(AddContactYesNoPage, true)
              .setValue(LocationOfGoodsContactSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(IdentificationPage, qualifierOfIdentification.copy(qualifier = CustomsOfficeIdentifier))

            result.get(IdentificationPage) mustBe defined
            result.get(InferredIdentificationPage) must not be defined
            result.get(LocationOfGoodsIdentifierSection) must not be defined
            result.get(AddContactYesNoPage) must not be defined
            result.get(LocationOfGoodsContactSection) must not be defined
        }
      }
    }
  }
}

class InferredIdentificationPageSpec extends PageBehaviours {

  "InferredIdentificationPage" - {

    beRetrievable[LocationOfGoodsIdentification](InferredIdentificationPage)

    beSettable[LocationOfGoodsIdentification](InferredIdentificationPage)

    beRemovable[LocationOfGoodsIdentification](InferredIdentificationPage)

    "cleanup" - {
      "must clean up LocationOfGoodsIdentifierSection and non-inferred value" in {
        forAll(arbitrary[LocationOfGoodsIdentification].retryUntil(_.code != CustomsOfficeIdentifier)) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage, qualifierOfIdentification)
              .setValue(LocationOfGoodsIdentifierSection, Json.obj("foo" -> "bar"))
              .setValue(AddContactYesNoPage, true)
              .setValue(LocationOfGoodsContactSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(InferredIdentificationPage, qualifierOfIdentification)

            result.get(InferredIdentificationPage) mustBe defined
            result.get(IdentificationPage) must not be defined
            result.get(LocationOfGoodsIdentifierSection) must not be defined
            result.get(AddContactYesNoPage) mustBe defined
            result.get(LocationOfGoodsContactSection) mustBe defined
        }
      }

      "must clean up contact when customs office identifier chosen" in {
        forAll(arbitrary[LocationOfGoodsIdentification]) {
          qualifierOfIdentification =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage, qualifierOfIdentification)
              .setValue(LocationOfGoodsIdentifierSection, Json.obj("foo" -> "bar"))
              .setValue(AddContactYesNoPage, true)
              .setValue(LocationOfGoodsContactSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(InferredIdentificationPage, qualifierOfIdentification.copy(qualifier = CustomsOfficeIdentifier))

            result.get(InferredIdentificationPage) mustBe defined
            result.get(IdentificationPage) must not be defined
            result.get(LocationOfGoodsIdentifierSection) must not be defined
            result.get(AddContactYesNoPage) must not be defined
            result.get(LocationOfGoodsContactSection) must not be defined
        }
      }
    }
  }
}
