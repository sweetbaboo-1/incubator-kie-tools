/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.databinding.rebind;

import java.util.Collection;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.client.container.RefHolder;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCConfigProcessor;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ui.shared.api.annotations.Model;

/**
 * The purpose of this IOC extension is to provide bean instances of bindable types that are
 * qualified with {@link Model} and to expose the {@link DataBinder} that manages the model
 * instance in a {@link RefHolder}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@IOCExtension
public class DataBindingIOCExtension implements IOCExtensionConfigurator {
  @Override
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext, 
      final IOCConfigProcessor procFactory) {}

  @Override
  @SuppressWarnings("rawtypes")
  public void afterInitialization(final IOCProcessingContext context, InjectionContext injectionContext,
      IOCConfigProcessor procFactory) {

    final Collection<MetaClass> allBindableTypes = DataBindingUtil.getAllBindableTypes(context.getGeneratorContext());

    for (final MetaClass modelBean : allBindableTypes) {
      injectionContext.registerInjector(new AbstractInjector() {
        {
          this.qualifyingMetadata =
              context.getQualifyingMetadataFactory().createFrom(DataBindingUtil.MODEL_QUALIFICATION);
        }

        @Override
        public void renderProvider(InjectableInstance injectableInstance) {}

        @Override
        public Statement getBeanInstance(InjectableInstance injectableInstance) {
          final String dataBinderVar = modelBean.getName() + "_DataBinder";
          final MetaClass binderClass
              = MetaClassFactory.parameterizedAs(DataBinder.class, MetaClassFactory.typeParametersOf(modelBean));
          context.append(
              Stmt.declareFinalVariable(dataBinderVar, binderClass, 
                  Stmt.invokeStatic(DataBinder.class,"forType", modelBean)));
          context.append(Stmt.loadVariable(BoundDecorator.HOLDER_VAR_NAME).invoke("set", Refs.get(dataBinderVar)));
          return Stmt.loadVariable(dataBinderVar).invoke("getModel");
        }

        @Override
        public MetaClass getInjectedType() {
          return modelBean;
        }
      });
    }
  }
}
