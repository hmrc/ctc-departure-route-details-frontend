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

package viewModels.exit

import base.SpecBase
import generators.Generators
import models.Mode
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.exit.AddCustomsOfficeOfExitYesNoPage
import pages.exit.index.{OfficeOfExitCountryPage, OfficeOfExitPage}
import viewModels.exit.ExitAnswersViewModel.ExitAnswersViewModelProvider

class ExitAnswersViewModelSpec extends SpecBase with Generators {

  "must return sections" in {
    val mode = arbitrary[Mode].sample.value

    val userAnswers = emptyUserAnswers
      .setValue(AddCustomsOfficeOfExitYesNoPage, arbitrary[Boolean].sample.value)
      .setValue(OfficeOfExitCountryPage(index), arbitrary[Country].sample.value)
      .setValue(OfficeOfExitPage(index), arbitrary[CustomsOffice].sample.value)

    val viewModelProvider = injector.instanceOf[ExitAnswersViewModelProvider]
    val sections          = viewModelProvider.apply(userAnswers, mode).sections

    sections.size mustEqual 2
    sections(1).rows.size mustEqual 1
    sections(1).addAnotherLink must be(defined)
  }

}
