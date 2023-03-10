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

import models.{LocationOfGoodsIdentification, LocationType}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class LocationTypePageSpec extends PageBehaviours {

  "LocationTypePage" - {

    beRetrievable[LocationType](LocationTypePage)

    beSettable[LocationType](LocationTypePage)

    beRemovable[LocationType](LocationTypePage)

    "cleanup" - {
      "when value changes" - {
        "must remove location of goods identifier" in {
          forAll(arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification]) {
            (firstLocationType, locationIdentifierType) =>
              forAll(arbitrary[LocationType].retryUntil(_ != firstLocationType)) {
                secondLocationType =>
                  val userAnswers = emptyUserAnswers
                    .setValue(LocationTypePage, firstLocationType)
                    .setValue(IdentificationPage, locationIdentifierType)
                    .setValue(InferredIdentificationPage, locationIdentifierType)

                  val result = userAnswers.setValue(LocationTypePage, secondLocationType)

                  result.get(IdentificationPage) must not be defined
                  result.get(InferredIdentificationPage) must not be defined
              }
          }
        }
      }

      "when value doesn't change" - {
        "must do nothing" in {
          forAll(arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification]) {
            (locationType, locationIdentifierType) =>
              val userAnswers = emptyUserAnswers
                .setValue(LocationTypePage, locationType)
                .setValue(IdentificationPage, locationIdentifierType)
                .setValue(InferredIdentificationPage, locationIdentifierType)

              val result = userAnswers.setValue(LocationTypePage, locationType)

              result.get(IdentificationPage) mustBe defined
              result.get(InferredIdentificationPage) mustBe defined
          }
        }
      }
    }
  }
}
