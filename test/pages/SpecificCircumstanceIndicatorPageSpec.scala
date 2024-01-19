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

package pages

import models.reference.SpecificCircumstanceIndicator
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.loadingAndUnloading.AddPlaceOfUnloadingPage

class SpecificCircumstanceIndicatorPageSpec extends PageBehaviours {

  "SpecificCircumstanceIndicatorPage" - {

    beRetrievable[SpecificCircumstanceIndicator](SpecificCircumstanceIndicatorPage)

    beSettable[SpecificCircumstanceIndicator](SpecificCircumstanceIndicatorPage)

    beRemovable[SpecificCircumstanceIndicator](SpecificCircumstanceIndicatorPage)

    "cleanup" - {
      "must remove add place of unloading yes/no" - {
        "when answer changes" in {
          forAll(arbitrary[SpecificCircumstanceIndicator]) {
            sci1 =>
              forAll(arbitrary[SpecificCircumstanceIndicator].retryUntil(_ != sci1)) {
                sci2 =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SpecificCircumstanceIndicatorPage, sci1)
                    .setValue(AddPlaceOfUnloadingPage, true)

                  val result = userAnswers.setValue(SpecificCircumstanceIndicatorPage, sci2)

                  result.get(AddPlaceOfUnloadingPage) must not be defined
              }
          }
        }
      }

      "must do nothing" - {
        "when answer does not change" in {
          forAll(arbitrary[SpecificCircumstanceIndicator]) {
            sci =>
              val userAnswers = emptyUserAnswers
                .setValue(SpecificCircumstanceIndicatorPage, sci)
                .setValue(AddPlaceOfUnloadingPage, true)

              val result = userAnswers.setValue(SpecificCircumstanceIndicatorPage, sci)

              result.get(AddPlaceOfUnloadingPage) mustBe defined
          }
        }
      }
    }
  }
}
