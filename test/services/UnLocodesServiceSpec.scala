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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.UnLocode
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnLocodesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new UnLocodesService(mockRefDataConnector)

  private val unLocode: UnLocode = UnLocode("ADALV", "Andorra la Vella")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "UnLocodesService" - {

    "doesUnLocodeExist" - {
      "must return true" - {
        "when UN/LOCODE exists" in {

          when(mockRefDataConnector.getUnLocode(any())(any(), any()))
            .thenReturn(Future.successful(Right(unLocode)))

          service.doesUnLocodeExist(unLocode.unLocodeExtendedCode).futureValue mustEqual true

          verify(mockRefDataConnector).getUnLocode(eqTo(unLocode.unLocodeExtendedCode))(any(), any())
        }
      }

      "must return false" - {
        "when UN/LOCODE does not exist in reference data" in {

          when(mockRefDataConnector.getUnLocode(any())(any(), any()))
            .thenReturn(Future.successful(Left(NoReferenceDataFoundException(""))))

          service.doesUnLocodeExist(unLocode.unLocodeExtendedCode).futureValue mustEqual false

          verify(mockRefDataConnector).getUnLocode(eqTo(unLocode.unLocodeExtendedCode))(any(), any())
        }
      }
    }
  }
}
