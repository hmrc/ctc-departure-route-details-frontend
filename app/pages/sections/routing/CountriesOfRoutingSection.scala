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

import models.Index
import models.journeyDomain.{ReaderSuccess, *}
import pages.AddAnotherPage
import pages.routing.AddAnotherCountryOfRoutingPage
import pages.routing.index.{CountryOfRoutingInCL112Page, CountryOfRoutingInCL147Page}
import pages.sections.AddAnotherSection
import play.api.libs.json.JsPath

case object CountriesOfRoutingSection extends AddAnotherSection {

  override def path: JsPath = RoutingSection.path \ toString

  override def toString: String = "countriesOfRouting"

  override val addAnotherPage: AddAnotherPage = AddAnotherCountryOfRoutingPage

  def atLeastOneCountryOfRoutingIsInCL147: Read[Boolean] =
    this.arrayReader.apply(_).map(_.to(_.value.length)).flatMap {
      case ReaderSuccess(numberOfCountriesOfRouting, pages) =>
        (0 until numberOfCountriesOfRouting)
          .foldLeft(UserAnswersReader.success(false)) {
            (acc, index) =>
              (
                acc,
                CountryOfRoutingInCL147Page(Index(index)).reader
              ).to {
                case (areAnyCountriesOfRoutingInCL147SoFar, isThisCountryOfRoutingInCL147) =>
                  pages =>
                    val result = areAnyCountriesOfRoutingInCL147SoFar || isThisCountryOfRoutingInCL147
                    ReaderSuccess(result, pages).toUserAnswersReader
              }
          }
          .apply(pages)
    }

  def anyCountriesOfRoutingInCL112: Read[Boolean] =
    this.arrayReader.apply(_).map(_.to(_.value.length)).flatMap {
      case ReaderSuccess(numberOfCountriesOfRouting, pages) =>
        (0 until numberOfCountriesOfRouting)
          .foldLeft(UserAnswersReader.success(false)) {
            (acc, index) =>
              (
                acc,
                CountryOfRoutingInCL112Page(Index(index)).reader
              ).to {
                case (areAnyCountriesOfRoutingInCL112SoFar, isThisCountryOfRoutingInCL112) =>
                  pages =>
                    val result = areAnyCountriesOfRoutingInCL112SoFar || isThisCountryOfRoutingInCL112
                    ReaderSuccess(result, pages).toUserAnswersReader
              }
          }
          .apply(pages)
    }
}
