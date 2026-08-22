package com.nexoranews.app.data.local

import com.nexoranews.app.data.model.Category
import com.nexoranews.app.data.model.NewsItem

object InitialNewsSeed {

    fun getDefaultCategories(): List<Category> = listOf(
        Category(
            id = "cat-angola",
            name = "Angola",
            slug = "angola",
            description = "Acontecimentos nacionais, províncias e sociedade angolana",
            color = "#E53935",
            order = 1,
            count = 5
        ),
        Category(
            id = "cat-politica",
            name = "Política",
            slug = "politica",
            description = "Governação, Assembleia Nacional, diplomacia e partidos",
            color = "#3B82F6",
            order = 2,
            count = 4
        ),
        Category(
            id = "cat-economia",
            name = "Economia",
            slug = "economia",
            description = "Mercados, finanças, hidrocarbonetos, banca e empreendedorismo",
            color = "#10B981",
            order = 3,
            count = 4
        ),
        Category(
            id = "cat-sociedade",
            name = "Sociedade",
            slug = "sociedade",
            description = "Educação, saúde, cultura e cidadania",
            color = "#8B5CF6",
            order = 4,
            count = 3
        ),
        Category(
            id = "cat-desporto",
            name = "Desporto",
            slug = "desporto",
            description = "Girabola, selecções nacionais, futebol internacional e basquetebol",
            color = "#F59E0B",
            order = 5,
            count = 3
        ),
        Category(
            id = "cat-tecnologia",
            name = "Tecnologia",
            slug = "tecnologia",
            description = "Inovação digital, telecomunicações, startups e ciência",
            color = "#06B6D4",
            order = 6,
            count = 3
        ),
        Category(
            id = "cat-mundo",
            name = "Mundo",
            slug = "mundo",
            description = "África Austral, CPLP e actualidade internacional",
            color = "#6366F1",
            order = 7,
            count = 2
        )
    )

    fun getDefaultNews(): List<NewsItem> = listOf(
        NewsItem(
            id = "seed-news-1",
            title = "Angola acelera diversificação económica com novos investimentos no sector agroindustrial",
            slug = "angola-acelera-diversificacao-economica-agroindustria",
            summary = "O Governo e investidores privados anunciaram pacotes de financiamento destinados ao aumento da produção nacional e escoamento de colheitas nas províncias do Huambo, Bié e Cuanza Sul.",
            content = """
                O Executivo angolano e parceiros internacionais formalizaram esta semana um conjunto de medidas e linhas de crédito estratégico focadas no fortalecimento do tecido agroindustrial nas províncias do centro e sul do país.

                A iniciativa tem como meta prioritária a redução sistemática da dependência de importações de bens essenciais da cesta básica, promovendo a autossuficiência alimentar e a criação de mais de 15 mil postos de trabalho directos no meio rural.

                Segundo dados apresentados pelo Ministério da Economia e Planeamento, as infraestruturas de armazenamento, silos e vias secundárias de acesso a campos agrícolas receberão especial atenção, permitindo que a produção local chegue com maior celeridade e menores custos aos grandes centros urbanos, incluindo Luanda.

                Especialistas do sector consideram o momento crucial para a consolidação de cadeias de valor integradas, sublinhando que a conjugação de incentivos fiscais e modernização tecnológica constitui o caminho mais sustentável para a resiliência económica nacional.
            """.trimIndent(),
            category = "Economia",
            imageUrl = "https://images.unsplash.com/photo-1595974482597-4b8da8879bc5?w=800&auto=format&fit=crop&q=80",
            imageCaption = "Produção agrícola e agroindustrial ganham impulso em Angola",
            author = "Redacção Nexora",
            source = "Nexora News",
            publishedAt = "2026-08-21T08:30:00Z",
            readTime = 4,
            isBreaking = true,
            isFeatured = true,
            views = 1420,
            tags = listOf("Economia", "Agricultura", "Angola", "Produção Nacional")
        ),
        NewsItem(
            id = "seed-news-2",
            title = "Assembleia Nacional debate novo quadro legislativo para reforço da transição digital",
            slug = "assembleia-nacional-debate-transicao-digital",
            summary = "Parlamentares analisam propostas de lei com vista a modernizar a administração pública, aumentar a cibersegurança e simplificar serviços para o cidadão.",
            content = """
                A Assembleia Nacional deu início à discussão especializada sobre os diplomas legais que compõem o novo Pacote de Modernização e Governação Electrónica de Angola.

                O projecto legislativo visa estabelecer directrizes unificadas para a interoperabilidade dos sistemas de informação governamentais, garantindo maior transparência, redução de burocracia e celeridade na emissão de documentos oficiais.

                Durante as sessões plenárias, deputados de várias bancadas destacaram a relevância de salvaguardar a protecção de dados pessoais e implementar normas rigorosas de segurança cibernética nas infraestruturas críticas do Estado.

                A promulgação do novo diploma é aguardada com expectativa pela comunidade empresarial e tecnológica, que antevê ganhos substanciais em competitividade e inclusão digital para todos os cidadãos.
            """.trimIndent(),
            category = "Política",
            imageUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=800&auto=format&fit=crop&q=80",
            imageCaption = "Sessão de trabalho parlamentar em Luanda",
            author = "Manuel de Sousa",
            source = "Nexora News",
            publishedAt = "2026-08-21T07:15:00Z",
            readTime = 3,
            isBreaking = false,
            isFeatured = false,
            views = 980,
            tags = listOf("Política", "Assembleia Nacional", "Digital", "Legislação")
        ),
        NewsItem(
            id = "seed-news-3",
            title = "Universidades angolanas expandem programas de bolsas e investigação científica",
            slug = "universidades-angolanas-expandem-bolsas-investigacao",
            summary = "Novas parcerias internacionais vão financiar projectos de investigação em áreas como energias renováveis, saúde pública e inteligência artificial aplicada.",
            content = """
                Instituições de ensino superior em Angola anunciaram a ampliação de programas de fomento à investigação científica, beneficiando estudantes e docentes com bolsas de estudo e acesso a laboratórios equipados.

                Os projectos seleccionados incidem primordialmente sobre os desafios contemporâneos da sustentabilidade ambiental, conservação hídrica e desenvolvimento de soluções tecnológicas adequadas à realidade africana.

                O reitorado destacou que a aposta no talento jovem é o alicerce fundamental para a soberania do conhecimento e inovação contínua nas próximas décadas.
            """.trimIndent(),
            category = "Sociedade",
            imageUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=800&auto=format&fit=crop&q=80",
            imageCaption = "Estudantes em ambiente de investigação académica",
            author = "Helena Fernandes",
            source = "Nexora News",
            publishedAt = "2026-08-20T16:45:00Z",
            readTime = 3,
            isBreaking = false,
            isFeatured = false,
            views = 760,
            tags = listOf("Sociedade", "Educação", "Ciência", "Universidade")
        ),
        NewsItem(
            id = "seed-news-4",
            title = "Girabola: Ronda decisiva promete emoções fortes na luta pelos lugares de topo",
            slug = "girabola-ronda-decisiva-topo-tabela",
            summary = "Os principais candidatos ao título defrontam-se este fim-de-semana em jogos cruciais com estádios lotados previstos em várias províncias.",
            content = """
                O Campeonato Nacional de Futebol da Primeira Divisão entra na sua fase mais vibrante, com os clubes cimeiros separados por uma margem estreita de pontos.

                As equipas intensificam os preparativos técnicos e tácticos para os clássicos da jornada, enquanto os adeptos esgotam os bilhetes nos principais recintos desportivos do país.

                A Federação Angolana de Futebol garantiu a prontidão de todos os dispositivos de arbitragem, segurança e transmissão televisiva para assegurar um espectáculo desportivo exemplar.
            """.trimIndent(),
            category = "Desporto",
            imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&auto=format&fit=crop&q=80",
            imageCaption = "Atmosfera dos estádios no futebol nacional",
            author = "Carlos Baptista",
            source = "Nexora Desporto",
            publishedAt = "2026-08-20T14:20:00Z",
            readTime = 3,
            isBreaking = false,
            isFeatured = false,
            views = 1890,
            tags = listOf("Desporto", "Girabola", "Futebol", "Angola")
        ),
        NewsItem(
            id = "seed-news-5",
            title = "Startup angolana vence prémio internacional de inovação em tecnologia financeira",
            slug = "startup-angolana-premio-fintech-inovacao",
            summary = "Plataforma desenvolvida em Luanda facilita micropagamentos e inclusão financeira para comerciantes informais e pequenas empresas.",
            content = """
                Uma startup fundada por jovens engenheiros de software em Luanda conquistou o primeiro lugar numa prestigiada cimeira de empreendedorismo e inovação digital em África.

                A solução desenvolvida permite a realização de pagamentos rápidos e gestão de tesouraria de forma simplificada, funcionando mesmo em áreas com ligação de dados intermitente.

                O júri elogiou a robustez da arquitectura técnica e o impacto social directo na integração de pequenos comerciantes no sistema financeiro formal.
            """.trimIndent(),
            category = "Tecnologia",
            imageUrl = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format&fit=crop&q=80",
            imageCaption = "Ecossistema de tecnologia e inovação em crescimento",
            author = "Teresa Gaspar",
            source = "Nexora Tech",
            publishedAt = "2026-08-20T11:10:00Z",
            readTime = 4,
            isBreaking = false,
            isFeatured = false,
            views = 1120,
            tags = listOf("Tecnologia", "Fintech", "Startups", "Inovação")
        )
    )
}
