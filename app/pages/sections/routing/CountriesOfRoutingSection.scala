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
import models.domain.{GettableAsReaderOps, JsArrayGettableAsReaderOps, UserAnswersReader}
import pages.routing.index.{CountryOfRoutingInCL112Page, CountryOfRoutingInCL147Page}
import pages.sections.Section
import play.api.libs.json.{JsArray, JsPath}

case object CountriesOfRoutingSection extends Section[JsArray] {

  override def path: JsPath = RoutingSection.path \ toString

  override def toString: String = "countriesOfRouting"

  def atLeastOneCountryOfRoutingNotInCL147: UserAnswersReader[Boolean] =
    for {
      numberOfCountriesOfRouting <- this.arrayReader.map(_.value.length)
      reader <- (0 until numberOfCountriesOfRouting).foldLeft(UserAnswersReader(false)) {
        (acc, index) =>
          for {
            areAnyCountriesOfRoutingNotInCL147SoFar <- acc
            isThisCountryOfRoutingNotInCL147        <- CountryOfRoutingInCL147Page(Index(index)).reader.map(!_)
          } yield areAnyCountriesOfRoutingNotInCL147SoFar || isThisCountryOfRoutingNotInCL147
      }
    } yield reader

  def anyCountriesOfRoutingInCL112: UserAnswersReader[Boolean] =
    for {
      numberOfCountriesOfRouting <- this.arrayReader.map(_.value.length)
      reader <- (0 until numberOfCountriesOfRouting).foldLeft(UserAnswersReader(false)) {
        (acc, index) =>
          for {
            areAnyCountriesOfRoutingInCL112SoFar <- acc
            isThisCountryOfRoutingInCL112        <- CountryOfRoutingInCL112Page(Index(index)).reader
          } yield areAnyCountriesOfRoutingInCL112SoFar || isThisCountryOfRoutingInCL112
      }
    } yield reader
}
