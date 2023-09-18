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

package pages.loadingAndUnloading.loading

import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddUnLocodeYesNoPageSpec extends PageBehaviours {

  "AddUnLocodeYesNoPage" - {

    beRetrievable[Boolean](AddUnLocodeYesNoPage)

    beSettable[Boolean](AddUnLocodeYesNoPage)

    beRemovable[Boolean](AddUnLocodeYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up Loading section" in {
          forAll(arbitrary[String], nonEmptyString, arbitrary[Country]) {
            (unLocode, location, country) =>
              val preChange = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, true)
                .setValue(UnLocodePage, unLocode)
                .setValue(AddExtraInformationYesNoPage, true)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val postChange = preChange.setValue(AddUnLocodeYesNoPage, false)

              postChange.get(UnLocodePage) mustNot be(defined)
              postChange.get(AddExtraInformationYesNoPage) mustNot be(defined)
              postChange.get(CountryPage) mustNot be(defined)
              postChange.get(LocationPage) mustNot be(defined)
          }
        }
      }
      "when YES selected" - {
        "must clean up Loading section" in {
          forAll(nonEmptyString, arbitrary[Country]) {
            (location, country) =>
              val preChange = emptyUserAnswers
                .setValue(AddUnLocodeYesNoPage, false)
                .setValue(CountryPage, country)
                .setValue(LocationPage, location)

              val postChange = preChange.setValue(AddUnLocodeYesNoPage, true)

              postChange.get(CountryPage) mustNot be(defined)
              postChange.get(LocationPage) mustNot be(defined)
          }
        }
      }

    }
  }
}
