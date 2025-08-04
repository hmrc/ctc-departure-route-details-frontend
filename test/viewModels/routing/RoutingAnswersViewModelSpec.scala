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

package viewModels.routing

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Mode
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.routing.index.CountryOfRoutingPage
import pages.routing.{AddCountryOfRoutingYesNoPage, BindingItineraryPage, OfficeOfDestinationPage}
import viewModels.routing.RoutingAnswersViewModel.RoutingAnswersViewModelProvider

class RoutingAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  "must return sections" in {
    val mode = arbitrary[Mode].sample.value

    val userAnswers = emptyUserAnswers
      .setValue(OfficeOfDestinationPage, arbitrary[CustomsOffice].sample.value)
      .setValue(BindingItineraryPage, arbitrary[Boolean].sample.value)
      .setValue(AddCountryOfRoutingYesNoPage, arbitrary[Boolean].sample.value)
      .setValue(CountryOfRoutingPage(index), arbitrary[Country].sample.value)

    val viewModelProvider = new RoutingAnswersViewModelProvider()

    val sections = viewModelProvider.apply(userAnswers, mode).sections

    sections.size mustEqual 2

    sections.head.sectionTitle mustNot be(defined)
    sections.head.rows.size mustEqual 3
    sections.head.addAnotherLink mustNot be(defined)

    sections(1).sectionTitle.get mustEqual "Transit route countries"
    sections(1).rows.size mustEqual 1
    sections(1).addAnotherLink must be(defined)
  }
}
