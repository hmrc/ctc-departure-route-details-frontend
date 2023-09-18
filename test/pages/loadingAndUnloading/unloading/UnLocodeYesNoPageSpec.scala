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

package pages.loadingAndUnloading.unloading

import models.reference.{Country, UnLocode}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.loadingAndUnloading.loading.AddUnLocodeYesNoPage

class UnLocodeYesNoPageSpec extends PageBehaviours {

  "AddUnLocodeYesNoPage" - {

    beRetrievable[Boolean](UnLocodeYesNoPage)

    beSettable[Boolean](UnLocodeYesNoPage)

    beRemovable[Boolean](UnLocodeYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up UnLoading section" in {
          forAll(arbitrary[UnLocode], nonEmptyString, arbitrary[Country]) {
            (unLocode, location, country) =>
              val preChange = emptyUserAnswers
                .setValue(UnLocodeYesNoPage, true)
                .setValue(UnLocodePage, unLocode)
                .setValue(AddExtraInformationYesNoPage, true)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val postChange = preChange.setValue(UnLocodeYesNoPage, false)

              postChange.get(UnLocodePage) mustNot be(defined)
              postChange.get(AddExtraInformationYesNoPage) mustNot be(defined)
              postChange.get(CountryPage) mustNot be(defined)
              postChange.get(LocationPage) mustNot be(defined)
          }
        }
      }
      "when YES selected" - {
        "must clean up UnLoading section" in {
          forAll(nonEmptyString, arbitrary[Country]) {
            (location, country) =>
              val preChange = emptyUserAnswers
                .setValue(UnLocodeYesNoPage, false)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val postChange = preChange.setValue(UnLocodeYesNoPage, true)

              postChange.get(CountryPage) mustNot be(defined)
              postChange.get(LocationPage) mustNot be(defined)
          }
        }
      }

    }
  }
}
