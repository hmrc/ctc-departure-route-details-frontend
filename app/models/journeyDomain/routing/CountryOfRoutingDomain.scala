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

package models.journeyDomain.routing

import models.journeyDomain.Stage._
import models.journeyDomain._
import models.reference.Country
import models.{Index, Mode, UserAnswers}
import pages.routing.index.CountryOfRoutingPage
import pages.sections.routing.CountriesOfRoutingSection
import play.api.mvc.Call

case class CountryOfRoutingDomain(
  country: Country
)(index: Index)
    extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    stage match {
      case AccessingJourney =>
        Some(controllers.routing.index.routes.CountryOfRoutingController.onPageLoad(userAnswers.lrn, mode, index))
      case CompletingJourney =>
        CountriesOfRoutingSection.route(userAnswers, mode)
    }
}

object CountryOfRoutingDomain {

  implicit def userAnswersReader(index: Index): Read[CountryOfRoutingDomain] =
    CountryOfRoutingPage(index).reader.map(CountryOfRoutingDomain(_)(index))
}
