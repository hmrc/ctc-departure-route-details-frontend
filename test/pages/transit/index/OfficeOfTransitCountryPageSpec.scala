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

import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class OfficeOfTransitCountryPageSpec extends PageBehaviours {

  "OfficeOfTransitCountryPage" - {

    beRetrievable[Country](OfficeOfTransitCountryPage(index))

    beSettable[Country](OfficeOfTransitCountryPage(index))

    beRemovable[Country](OfficeOfTransitCountryPage(index))
  }

  "cleanup" - {

    "must clean up Office Of Transit page and inferred value" in {
      forAll(arbitrary[Country], arbitrary[CustomsOffice]) {
        (country, customsOffice) =>
          val userAnswers = emptyUserAnswers
            .setValue(InferredOfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), customsOffice)

          val result = userAnswers.setValue(OfficeOfTransitCountryPage(index), country)

          result.get(OfficeOfTransitCountryPage(index)) mustBe defined
          result.get(InferredOfficeOfTransitCountryPage(index)) must not be defined
          result.get(OfficeOfTransitPage(index)) must not be defined
      }
    }
  }
}

class InferredOfficeOfTransitCountryPageSpec extends PageBehaviours {

  "InferredOfficeOfTransitCountryPage" - {

    beRetrievable[Country](InferredOfficeOfTransitCountryPage(index))

    beSettable[Country](InferredOfficeOfTransitCountryPage(index))

    beRemovable[Country](InferredOfficeOfTransitCountryPage(index))
  }

  "cleanup" - {

    "must clean up Office Of Transit page and non-inferred value" in {
      forAll(arbitrary[Country], arbitrary[CustomsOffice]) {
        (country, customsOffice) =>
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), customsOffice)

          val result = userAnswers.setValue(InferredOfficeOfTransitCountryPage(index), country)

          result.get(InferredOfficeOfTransitCountryPage(index)) mustBe defined
          result.get(OfficeOfTransitCountryPage(index)) must not be defined
          result.get(OfficeOfTransitPage(index)) must not be defined
      }
    }
  }
}
