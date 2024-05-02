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

package controllers.actions

import config.FrontendAppConfig
import models.LocalReferenceNumber
import models.requests._
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRequiredActionImpl @Inject() (config: FrontendAppConfig, sessionRepository: SessionRepository)(implicit val executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] =

}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]{

  def apply(lrn: LocalReferenceNumber): ActionRefiner[OptionalDataRequest, DataRequest]
}

class DataRequiredAction @Inject() (config: FrontendAppConfig, sessionRepository: SessionRepository) extends DataRequiredAction {
  override def apply(lrn: LocalReferenceNumber): ActionRefiner[OptionalDataRequest, DataRequest] =
    new DataRequiredAction(lrn, sessionRepository)
  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = request.userAnswers match {
    case None =>
      Future.successful(Left(Redirect(config.sessionExpiredUrl())))
    case Some(data) =>
      Future.successful(Right(DataRequest(request.request, request.eoriNumber, data)))
  }
}