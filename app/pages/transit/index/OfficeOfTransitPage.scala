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

import controllers.transit.index.routes
import models.reference.CustomsOffice
import models.{Index, Mode, UserAnswers}
import pages.{InferredPage, QuestionPage}
import pages.sections.exit.ExitSection
import pages.sections.transit.OfficeOfTransitSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class OfficeOfTransitPage(index: Index) extends QuestionPage[CustomsOffice] {

  override def path: JsPath = OfficeOfTransitSection(index).path \ toString

  override def toString: String = "officeOfTransit"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.OfficeOfTransitController.onPageLoad(userAnswers.lrn, mode, index))

  override def cleanup(value: Option[CustomsOffice], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(_) => userAnswers.remove(OfficeOfTransitETAPage(index)).flatMap(_.remove(ExitSection))
    case None    => super.cleanup(value, userAnswers)
  }

}

case class OfficeOfTransitInCL147Page(index: Index) extends InferredPage[Boolean] {

  override def path: JsPath = OfficeOfTransitPage(index).path \ toString

  override def toString: String = "isInCL147"
}

case class OfficeOfTransitInCL010Page(index: Index) extends InferredPage[Boolean] {

  override def path: JsPath = OfficeOfTransitPage(index).path \ toString

  override def toString: String = "isInCL010"
}
