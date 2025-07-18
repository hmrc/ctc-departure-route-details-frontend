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
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.transit.AddAnotherOfficeOfTransitViewModel.AddAnotherOfficeOfTransitViewModelProvider

class AddAnotherOfficeOfTransitViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one office of transit" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val userAnswers = arbitraryOfficeOfTransitAnswers(emptyUserAnswers, index).sample.value

          val result = new AddAnotherOfficeOfTransitViewModelProvider()(userAnswers, mode)

          result.listItems.length mustEqual 1
          result.title mustEqual "You have added 1 office of transit"
          result.heading mustEqual "You have added 1 office of transit"
          result.legend mustEqual "Do you want to add another office of transit?"
          result.maxLimitLabel mustEqual "You cannot add any more offices of transit. To add another office, you need to remove one first."
      }
    }

    "when there are multiple offices of transit" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], Gen.choose(2, frontendAppConfig.maxOfficesOfTransit)) {
        (mode, count) =>
          val userAnswers = (0 until count).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              arbitraryOfficeOfTransitAnswers(acc, Index(i)).sample.value
          }

          val result = new AddAnotherOfficeOfTransitViewModelProvider()(userAnswers, mode)

          result.listItems.length mustEqual count
          result.title mustEqual s"You have added ${formatter.format(count)} offices of transit"
          result.heading mustEqual s"You have added ${formatter.format(count)} offices of transit"
          result.legend mustEqual "Do you want to add another office of transit?"
          result.maxLimitLabel mustEqual "You cannot add any more offices of transit. To add another office, you need to remove one first."
      }
    }
  }

}
