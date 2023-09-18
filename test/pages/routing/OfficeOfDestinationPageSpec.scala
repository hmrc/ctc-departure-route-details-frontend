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

package pages.routing

import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary._
import pages.behaviours.PageBehaviours
import pages.transit.index.OfficeOfTransitCountryPage

class OfficeOfDestinationPageSpec extends PageBehaviours {

  "OfficeOfDestinationPage" - {

    beRetrievable[CustomsOffice](OfficeOfDestinationPage)

    beSettable[CustomsOffice](OfficeOfDestinationPage)

    beRemovable[CustomsOffice](OfficeOfDestinationPage)
  }

  "cleanup" - {
    "when value changes" - {

      "must clean up transit section" in {
        forAll(arbitrary[CustomsOffice], arbitrary[CustomsOffice], arbitrary[Country]) {
          (customsOffice1, customsOffice2, country) =>
            val preChange = emptyUserAnswers
              .setValue(OfficeOfDestinationPage, customsOffice1)
              .setValue(OfficeOfTransitCountryPage(index), country)

            val postChange = preChange.setValue(OfficeOfDestinationPage, customsOffice2)

            postChange.get(OfficeOfTransitCountryPage(index)) mustNot be(defined)
        }
      }
    }

    "when value has not changed" - {
      "must not clean up transit section" in {
        forAll(arbitrary[CustomsOffice], arbitrary[Country]) {
          (customsOffice1, country) =>
            val preChange = emptyUserAnswers
              .setValue(OfficeOfDestinationPage, customsOffice1)
              .setValue(OfficeOfTransitCountryPage(index), country)

            val postChange = preChange.setValue(OfficeOfDestinationPage, customsOffice1)

            postChange.get(OfficeOfTransitCountryPage(index)) mustBe defined
        }
      }
    }
  }
}
