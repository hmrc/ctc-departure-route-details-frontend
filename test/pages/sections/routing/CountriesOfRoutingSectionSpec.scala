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

package pages.sections.routing

import base.SpecBase
import models.Index
import pages.routing.index.{CountryOfRoutingInCL112Page, CountryOfRoutingInCL147Page}

class CountriesOfRoutingSectionSpec extends SpecBase {

  "atLeastOneCountryOfRoutingNotInCL147" - {
    "must return false" - {
      "when all countries of routing are in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL147Page(Index(0)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(1)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(2)), true)

        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingNotInCL147.run(userAnswers).value

        result mustBe false
      }

      "when no countries of routing" in {
        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingNotInCL147.run(emptyUserAnswers).value

        result mustBe false
      }
    }

    "must return true" - {
      "when no countries of routing are in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL147Page(Index(0)), false)
          .setValue(CountryOfRoutingInCL147Page(Index(1)), false)
          .setValue(CountryOfRoutingInCL147Page(Index(2)), false)

        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingNotInCL147.run(userAnswers).value

        result mustBe true
      }

      "when some countries of routing are not in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL147Page(Index(0)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(1)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(2)), false)

        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingNotInCL147.run(userAnswers).value

        result mustBe true
      }
    }
  }

  "atLeastOneCountryOfRoutingInCL147" - {
    "must return true" - {
      "when all countries of routing are in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL147Page(Index(0)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(1)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(2)), true)

        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingIsInCL147.run(userAnswers).value

        result mustBe true
      }

      "when some countries of routing are in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL147Page(Index(0)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(1)), true)
          .setValue(CountryOfRoutingInCL147Page(Index(2)), false)

        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingIsInCL147.run(userAnswers).value

        result mustBe true
      }
    }

    "must return false" - {
      "when no countries of routing are in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL147Page(Index(0)), false)
          .setValue(CountryOfRoutingInCL147Page(Index(1)), false)
          .setValue(CountryOfRoutingInCL147Page(Index(2)), false)

        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingIsInCL147.run(userAnswers).value

        result mustBe false
      }

      "when no countries of routing" in {
        val result = CountriesOfRoutingSection.atLeastOneCountryOfRoutingIsInCL147.run(emptyUserAnswers).value

        result mustBe false
      }
    }
  }

  "anyCountriesOfRoutingInCL112" - {
    "must return true" - {
      "when all countries of routing are in CL112" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL112Page(Index(0)), true)
          .setValue(CountryOfRoutingInCL112Page(Index(1)), true)
          .setValue(CountryOfRoutingInCL112Page(Index(2)), true)

        val result = CountriesOfRoutingSection.anyCountriesOfRoutingInCL112.run(userAnswers).value

        result mustBe true
      }

      "when some countries of routing are in CL112" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL112Page(Index(0)), false)
          .setValue(CountryOfRoutingInCL112Page(Index(1)), false)
          .setValue(CountryOfRoutingInCL112Page(Index(2)), true)

        val result = CountriesOfRoutingSection.anyCountriesOfRoutingInCL112.run(userAnswers).value

        result mustBe true
      }
    }

    "must return false" - {
      "when no countries of routing are in CL147" in {
        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingInCL112Page(Index(0)), false)
          .setValue(CountryOfRoutingInCL112Page(Index(1)), false)
          .setValue(CountryOfRoutingInCL112Page(Index(2)), false)

        val result = CountriesOfRoutingSection.anyCountriesOfRoutingInCL112.run(userAnswers).value

        result mustBe false
      }

      "when there are no countries of routing" in {
        val result = CountriesOfRoutingSection.anyCountriesOfRoutingInCL112.run(emptyUserAnswers).value

        result mustBe false
      }
    }
  }
}
