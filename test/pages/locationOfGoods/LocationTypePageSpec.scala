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
      "must remove location of goods identifier" - {
        "when not inferred" in {
          forAll(arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification]) {
            (locationType, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(IdentificationPage, identification)

              val result = userAnswers.setValue(LocationTypePage, locationType)

              result.get(IdentificationPage) must not be defined
          }
        }

        "when inferred" in {
          forAll(arbitrary[LocationType], arbitrary[LocationOfGoodsIdentification]) {
            (locationType, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(InferredIdentificationPage, identification)

              val result = userAnswers.setValue(LocationTypePage, locationType)

              result.get(InferredIdentificationPage) must not be defined
          }
        }
      }
    }
  }
}
