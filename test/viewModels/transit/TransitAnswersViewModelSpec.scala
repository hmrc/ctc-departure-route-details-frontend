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
import models.Mode
import org.scalacheck.Arbitrary.arbitrary
import pages.transit.{AddOfficeOfTransitYesNoPage, T2DeclarationTypeYesNoPage}
import viewModels.transit.TransitAnswersViewModel.TransitAnswersViewModelProvider

class TransitAnswersViewModelSpec extends SpecBase with Generators {

  "must return sections" in {
    val mode = arbitrary[Mode].sample.value

    val initialAnswers = emptyUserAnswers
      .setValue(T2DeclarationTypeYesNoPage, arbitrary[Boolean].sample.value)
      .setValue(AddOfficeOfTransitYesNoPage, arbitrary[Boolean].sample.value)

    val userAnswers = arbitraryOfficeOfTransitAnswers(initialAnswers, index).sample.value

    val viewModelProvider = injector.instanceOf[TransitAnswersViewModelProvider]
    val sections          = viewModelProvider.apply(userAnswers, mode).sections

    sections.size mustEqual 2

    sections.head.sectionTitle mustNot be(defined)
    sections.head.rows.size mustEqual 2
    sections.head.addAnotherLink mustNot be(defined)

    sections(1).sectionTitle.get mustEqual "Offices of transit"
    sections(1).rows.size mustEqual 1
    sections(1).addAnotherLink must be(defined)
  }
}
