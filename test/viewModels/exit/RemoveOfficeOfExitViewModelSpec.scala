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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Index
import models.reference.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.exit.index.OfficeOfExitPage
import viewModels.exit.RemoveOfficeOfExitViewModel.RemoveOfficeOfExitViewModelProvider

class RemoveOfficeOfExitViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val viewModelProvider = new RemoveOfficeOfExitViewModelProvider()

  "must create view model" - {

    "when office of exit undefined at index" in {
      val viewModel = viewModelProvider(emptyUserAnswers, Index(0))

      viewModel.officeOfExit mustBe None
      viewModel.title mustBe "Are you sure you want to remove this office of exit for transit?"
      viewModel.heading mustBe "Are you sure you want to remove this office of exit for transit?"
    }

    "when office of exit defined at index" in {
      forAll(arbitrary[CustomsOffice]) {
        officeOfExit =>
          val index       = Index(0)
          val userAnswers = emptyUserAnswers.setValue(OfficeOfExitPage(index), officeOfExit)
          val viewModel   = viewModelProvider(userAnswers, Index(0))

          viewModel.officeOfExit.value mustBe officeOfExit
          viewModel.title mustBe s"Are you sure you want to remove ${officeOfExit.name} as an office of exit for transit?"
          viewModel.heading mustBe s"Are you sure you want to remove ${officeOfExit.name} as an office of exit for transit?"
      }
    }
  }

}
