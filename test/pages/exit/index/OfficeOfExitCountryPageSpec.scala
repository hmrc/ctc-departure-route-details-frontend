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

package pages.exit.index

import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.exit

class OfficeOfExitCountryPageSpec extends PageBehaviours {

  "OfficeOfExitCountryPage" - {

    beRetrievable[Country](OfficeOfExitCountryPage(index))

    beSettable[Country](exit.index.OfficeOfExitCountryPage(index))

    beRemovable[Country](exit.index.OfficeOfExitCountryPage(index))

    "cleanup" - {
      "must remove inferred value" in {
        forAll(arbitrary[Country], arbitrary[CustomsOffice]) {
          (country, customsOffice) =>
            val userAnswers = emptyUserAnswers
              .setValue(InferredOfficeOfExitCountryPage(index), country)
              .setValue(OfficeOfExitPage(index), customsOffice)

            val result = userAnswers.setValue(OfficeOfExitCountryPage(index), country)

            result.get(InferredOfficeOfExitCountryPage(index)) must not be defined
            result.get(OfficeOfExitPage(index)) must not be defined
        }
      }
    }
  }
}
