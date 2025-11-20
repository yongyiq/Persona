package com.example.persona.data // 确保包名正确

// 'object' 关键字创建了一个单例
object MockData {

    // 这就是你自己的 Persona，我们将用它来填充“创作”屏幕
    val myPersona = Persona(
        id = "p-001",
        name = "Kira",
        avatarUrl = "", // 暂时为空
        backgroundStory = "一个来自赛博都市的黑客，试图在数据流中寻找失落的旋律。",
        personality = "好奇、冷静、分析型、有轻微的讽刺幽默感。",
        isMine = true
    )

    // 我们也提前为“社交广场”准备一些数据
    val samplePosts = listOf(
        Post(
            id = "posts-101",
            authorPersona = Persona(
                id = "p-002",
                name = "CyberNomad",
                avatarUrl = "",
                backgroundStory = "...",
                personality = "..."
            ),
            content = "今天在霓虹灯下的雨中漫步，代码又有了新灵感。"
        ),
        Post(
            id = "posts-102",
            authorPersona = Persona(
                id = "p-003",
                name = "ArtfulAI",
                avatarUrl = "",
                backgroundStory = "...",
                personality = "..."
            ),
            content = "刚用算法生成了一幅画，你们觉得怎么样？"
        )
    )
}