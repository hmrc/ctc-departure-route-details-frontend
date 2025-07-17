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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Index
import models.reference.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transit.index.OfficeOfTransitPage
import viewModels.transit.RemoveOfficeOfTransitViewModel.RemoveOfficeOfTransitViewModelProvider

class RemoveOfficeOfTransitViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val viewModelProvider = new RemoveOfficeOfTransitViewModelProvider()

  "must create view model" - {

    "when office of transit undefined at index" in {
      val viewModel = viewModelProvider(emptyUserAnswers, Index(0))

      viewModel.officeOfTransit must not be defined
      viewModel.title mustEqual "Are you sure you want to remove this office of transit?"
      viewModel.heading mustEqual "Are you sure you want to remove this office of transit?"
    }

    "when office of transit defined at index" in {
      forAll(arbitrary[CustomsOffice]) {
        officeOfTransit =>
          val index       = Index(0)
          val userAnswers = emptyUserAnswers.setValue(OfficeOfTransitPage(index), officeOfTransit)
          val viewModel   = viewModelProvider(userAnswers, Index(0))

          viewModel.officeOfTransit.value mustEqual officeOfTransit
          viewModel.title mustEqual s"Are you sure you want to remove this office of transit?"
          viewModel.heading mustEqual s"Are you sure you want to remove this office of transit?"
      }
    }
  }

}
