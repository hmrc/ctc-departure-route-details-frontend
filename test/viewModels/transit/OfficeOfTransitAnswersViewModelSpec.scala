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

package viewModels.transit

import base.SpecBase
import generators.Generators
import models.{DateTime, Mode}
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transit.index.{AddOfficeOfTransitETAYesNoPage, OfficeOfTransitCountryPage, OfficeOfTransitETAPage, OfficeOfTransitPage}
import viewModels.transit.OfficeOfTransitAnswersViewModel.OfficeOfTransitAnswersViewModelProvider

class OfficeOfTransitAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "apply" - {

    val mode = arbitrary[Mode].sample.value

    "must return row for each answer" - {

      "when ETA is not required" - {
        "must return 3 rows" in {
          forAll(arbitrary[Country], arbitrary[CustomsOffice]) {
            (country, office) =>
              val answers = emptyUserAnswers
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), office)
                .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

              val viewModelProvider = injector.instanceOf[OfficeOfTransitAnswersViewModelProvider]
              val section           = viewModelProvider.apply(answers, mode, index).section

              section.sectionTitle mustNot be(defined)
              section.rows.size mustEqual 3
          }
        }
      }

      "when ETA is required" - {
        "must return 4 rows" in {
          forAll(arbitrary[Country], arbitrary[CustomsOffice], arbitrary[DateTime]) {
            (country, office, dateTime) =>
              val answers = emptyUserAnswers
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), office)
                .setValue(AddOfficeOfTransitETAYesNoPage(index), true)
                .setValue(OfficeOfTransitETAPage(index), dateTime)

              val viewModelProvider = injector.instanceOf[OfficeOfTransitAnswersViewModelProvider]
              val section           = viewModelProvider.apply(answers, mode, index).section

              section.sectionTitle mustNot be(defined)
              section.rows.size mustEqual 4
          }
        }
      }
    }
  }
}
