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

class AddSpecificCircumstanceIndicatorYesNoPageSpec extends PageBehaviours {

  "AddSpecificCircumstanceIndicatorYesNoPage" - {

    beRetrievable[Boolean](AddSpecificCircumstanceIndicatorYesNoPage)

    beSettable[Boolean](AddSpecificCircumstanceIndicatorYesNoPage)

    beRemovable[Boolean](AddSpecificCircumstanceIndicatorYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must remove specific circumstance indicator" in {
          forAll(arbitrary[SpecificCircumstanceIndicator]) {
            specificCircumstanceIndicator =>
              val preChange = emptyUserAnswers
                .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

              val postChange = preChange.setValue(AddSpecificCircumstanceIndicatorYesNoPage, false)

              postChange.get(SpecificCircumstanceIndicatorPage) mustNot be(defined)
          }
        }
      }
    }
  }
}
