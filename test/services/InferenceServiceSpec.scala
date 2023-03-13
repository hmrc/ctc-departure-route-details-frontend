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

package services

import base.SpecBase
import generators.Generators
import models.{LocationOfGoodsIdentification, LocationType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.LocationTypePage

class InferenceServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val service = injector.instanceOf[InferenceService]

  "inferLocationOfGoodsIdentifier" - {

    "when location type is AuthorisedPlace" - {
      "must infer answer as AuthorisationNumber" in {
        val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType.AuthorisedPlace)
        service.inferLocationOfGoodsIdentifier(userAnswers) mustBe Some(LocationOfGoodsIdentification.AuthorisationNumber)
      }
    }

    "when location type is not AuthorisedPlace" - {
      "must not infer answer" in {
        forAll(arbitrary[LocationType].retryUntil(_ != LocationType.AuthorisedPlace)) {
          locationType =>
            val userAnswers = emptyUserAnswers.setValue(LocationTypePage, locationType)
            service.inferLocationOfGoodsIdentifier(userAnswers) mustBe None
        }
      }
    }
  }
}
