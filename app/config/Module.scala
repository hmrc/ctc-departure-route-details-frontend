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

package config

import com.google.inject.AbstractModule
import controllers.actions._
import navigation._

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[IdentifierAction]).to(classOf[IdentifierActionImpl]).asEagerSingleton()
    bind(classOf[DataRetrievalActionProvider]).to(classOf[DataRetrievalActionProviderImpl]).asEagerSingleton()
    bind(classOf[DataRequiredActionProvider]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[LockActionProvider]).to(classOf[LockActionProviderImpl]).asEagerSingleton()
    bind(classOf[DependentTasksAction]).to(classOf[DependentTasksActionImpl]).asEagerSingleton()
    bind(classOf[SpecificDataRequiredActionProvider]).to(classOf[SpecificDataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[IndexRequiredActionProvider]).to(classOf[IndexRequiredActionProviderImpl]).asEagerSingleton()

    bind(classOf[RouteDetailsNavigatorProvider]).to(classOf[RouteDetailsNavigatorProviderImpl])
    bind(classOf[RoutingNavigatorProvider]).to(classOf[RoutingNavigatorProviderImpl])
    bind(classOf[CountryOfRoutingNavigatorProvider]).to(classOf[CountryOfRoutingNavigatorProviderImpl])
    bind(classOf[TransitNavigatorProvider]).to(classOf[TransitNavigatorProviderImpl])
    bind(classOf[OfficeOfTransitNavigatorProvider]).to(classOf[OfficeOfTransitNavigatorProviderImpl])
    bind(classOf[ExitNavigatorProvider]).to(classOf[ExitNavigatorProviderImpl])
    bind(classOf[OfficeOfExitNavigatorProvider]).to(classOf[OfficeOfExitNavigatorProviderImpl])
    bind(classOf[LocationOfGoodsNavigatorProvider]).to(classOf[LocationOfGoodsNavigatorProviderImpl])
    bind(classOf[LoadingAndUnloadingNavigatorProvider]).to(classOf[LoadingAndUnloadingNavigatorProviderImpl])

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }
}
