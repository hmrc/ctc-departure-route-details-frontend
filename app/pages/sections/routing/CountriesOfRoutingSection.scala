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
import models.domain.{GettableAsReaderOps, JsArrayGettableAsReaderOps, Read, UserAnswersReader}
import models.journeyDomain.ReaderSuccess
import pages.routing.index.{CountryOfRoutingInCL112Page, CountryOfRoutingInCL147Page}
import pages.sections.Section
import play.api.libs.json.{JsArray, JsPath}

case object CountriesOfRoutingSection extends Section[JsArray] {

  override def path: JsPath = RoutingSection.path \ toString

  override def toString: String = "countriesOfRouting"

  def atLeastOneCountryOfRoutingIsInCL147: Read[Boolean] = pages => {
    this.arrayReader.apply(pages).map(_.to(_.value.length)).flatMap {
      case ReaderSuccess(numberOfCountriesOfRouting, pages) =>
        (0 until numberOfCountriesOfRouting).foldLeft(UserAnswersReader.success(false).apply(pages)) {
          (acc, index) =>
            acc.flatMap {
              case ReaderSuccess(areAnyCountriesOfRoutingInCL147SoFar, pages) =>
                CountryOfRoutingInCL147Page(Index(index)).reader.apply(pages).map {
                  case ReaderSuccess(isThisCountryOfRoutingInCL147, pages) =>
                    val result = areAnyCountriesOfRoutingInCL147SoFar || isThisCountryOfRoutingInCL147
                    ReaderSuccess(result, pages)
                }
            }
        }
    }
  }

  def anyCountriesOfRoutingInCL112: Read[Boolean] = pages =>
    this.arrayReader.apply(pages).map(_.to(_.value.length)).flatMap {
      case ReaderSuccess(numberOfCountriesOfRouting, pages) =>
        (0 until numberOfCountriesOfRouting).foldLeft(UserAnswersReader.success(false).apply(pages)) {
          (acc, index) =>
            acc.flatMap {
              case ReaderSuccess(areAnyCountriesOfRoutingInCL112SoFar, pages) =>
                CountryOfRoutingInCL112Page(Index(index)).reader.apply(pages).map {
                  case ReaderSuccess(isThisCountryOfRoutingInCL112, pages) =>
                    val result = areAnyCountriesOfRoutingInCL112SoFar || isThisCountryOfRoutingInCL112
                    ReaderSuccess(result, pages)

                }
            }
        }
    }
}
