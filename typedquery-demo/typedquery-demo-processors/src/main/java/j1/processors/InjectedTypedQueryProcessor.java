/*
 * Copyright (c) 2012, Michael Nascimento Santos & Michel Graciano.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the project's authors nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND/OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package j1.processors;

import j1.ci.QueryName;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.TypedQuery;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({"javax.inject.Inject"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class InjectedTypedQueryProcessor extends AbstractProcessor {

    private final Set<String> namedQueries = new HashSet<>();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
            final RoundEnvironment env) {
        if (env.processingOver()) {
            return false;
        }

        processCache(env);

        for (Element element : env.getElementsAnnotatedWith(QueryName.class)) {
            final QueryName queryName = element.getAnnotation(QueryName.class);

            if (!namedQueries.contains(queryName.value())) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Named query \'" + queryName.value() + "\' not defined yet.", element);
            }
        }

        final Elements elements = processingEnv.getElementUtils();
        final Types types = processingEnv.getTypeUtils();

        for (Element element : env.getElementsAnnotatedWith(Inject.class)) {
            if (element.getKind() != ElementKind.FIELD
                    || element.getAnnotation(QueryName.class) != null) {
                continue;
            }

            final TypeMirror type = element.asType();
            if (!types.isSameType(types.erasure(type), types.erasure(
                    elements.getTypeElement(TypedQuery.class.getCanonicalName())
                    .asType()))) {
                continue;
            }

            final EntityNameScanner ens = new EntityNameScanner();
            final String queryName = ens.scan(element) + "."
                    + element.getSimpleName().toString();

            if (!namedQueries.contains(queryName)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Named query \'" + queryName + "\' not defined yet.", element);
            }
        }

        return false;
    }

    private void processCache(final RoundEnvironment env) {
        final Filer filer = processingEnv.getFiler();

        final String resourceName = "cache_query_names";
        try {
            final FileObject resource = filer.getResource(
                    StandardLocation.SOURCE_OUTPUT, "", resourceName);
            try (BufferedReader reader = new BufferedReader(resource.
                    openReader(true))) {
                String line = reader.readLine();
                while (line != null) {
                    namedQueries.add(line);
                    line = reader.readLine();
                }
            } catch (FileNotFoundException ex) {
                /*
                 * Suppressed exception because it gonna happen always the clean
                 * target is executed before the build
                 */
            }

            for (Element element : env.getElementsAnnotatedWith(NamedQueries.class)) {
                final NamedQueries ann = element.getAnnotation(NamedQueries.class);
                for (NamedQuery namedQuery : ann.value()) {
                    namedQueries.add(namedQuery.name());
                }
            }

            try (Writer writer = filer.createResource(
                    StandardLocation.SOURCE_OUTPUT, "", resourceName).openWriter()) {
                for (String string : namedQueries) {
                    writer.append(string).append("\n");
                }
            }
        } catch (IOException ex) {
//            TODO: Need more investigation about why FilerException is thrown when running inside NetBeans editor
        }
    }
}
