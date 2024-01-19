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

package pages.routing.index

import controllers.routing.index.routes
import models.reference.Country
import models.{Index, Mode, UserAnswers}
import pages.{InferredPage, QuestionPage}
import pages.sections.exit.ExitSection
import pages.sections.routing.CountryOfRoutingSection
import pages.sections.transit.TransitSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class CountryOfRoutingPage(index: Index) extends QuestionPage[Country] {

  override def path: JsPath = CountryOfRoutingSection(index).path \ toString

  override def toString: String = "countryOfRouting"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.CountryOfRoutingController.onPageLoad(userAnswers.lrn, mode, index))

  override def cleanup(value: Option[Country], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(TransitSection).flatMap(_.remove(ExitSection))
      case None    => super.cleanup(value, userAnswers)
    }
}

case class CountryOfRoutingInCL112Page(index: Index) extends InferredPage[Boolean] {

  override def path: JsPath = CountryOfRoutingPage(index).path \ toString

  override def toString: String = "isInCL112"
}

case class CountryOfRoutingInCL147Page(index: Index) extends InferredPage[Boolean] {

  override def path: JsPath = CountryOfRoutingPage(index).path \ toString

  override def toString: String = "isInCL147"
}
