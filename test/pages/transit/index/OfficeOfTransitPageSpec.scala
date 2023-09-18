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

package pages.transit.index

import models.DateTime
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary._
import pages.behaviours.PageBehaviours
import pages.exit.index.OfficeOfExitCountryPage

class OfficeOfTransitPageSpec extends PageBehaviours {

  "OfficeOfTransitPage" - {

    beRetrievable[CustomsOffice](OfficeOfTransitPage(index))

    beSettable[CustomsOffice](OfficeOfTransitPage(index))

    beRemovable[CustomsOffice](OfficeOfTransitPage(index))
  }

  "cleanup" - {
    "when value changes" - {

      "must clean up transit eta page and exit section" in {
        forAll(arbitrary[CustomsOffice], arbitrary[CustomsOffice], arbitrary[DateTime], arbitrary[Country]) {
          (customsOffice1, customsOffice2, date, country) =>
            val preChange = emptyUserAnswers
              .setValue(OfficeOfTransitPage(index), customsOffice1)
              .setValue(OfficeOfTransitETAPage(index), date)
              .setValue(OfficeOfExitCountryPage(index), country)

            val postChange = preChange
              .setValue(OfficeOfTransitPage(index), customsOffice2)

            postChange.get(OfficeOfExitCountryPage(index)) mustNot be(defined)
            postChange.get(OfficeOfTransitETAPage(index)) mustNot be(defined)
        }
      }
    }

    "when value has not changed" - {
      "must not clean up transit eta page and exit section" in {
        forAll(arbitrary[CustomsOffice], arbitrary[DateTime], arbitrary[Country]) {
          (customsOffice1, date, country) =>
            val preChange = emptyUserAnswers
              .setValue(OfficeOfTransitPage(index), customsOffice1)
              .setValue(OfficeOfTransitETAPage(index), date)
              .setValue(OfficeOfExitCountryPage(index), country)

            val postChange = preChange
              .setValue(OfficeOfTransitPage(index), customsOffice1)

            postChange.get(OfficeOfExitCountryPage(index)) mustBe defined
            postChange.get(OfficeOfTransitETAPage(index)) mustBe defined
        }
      }
    }
  }
}
