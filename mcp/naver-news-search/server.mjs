import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const CLIENT_ID = process.env.NAVER_CLIENT_ID ?? "";
const CLIENT_SECRET = process.env.NAVER_CLIENT_SECRET ?? "";

const server = new McpServer({ name: "naver-news-search", version: "1.0.0" });

server.tool(
  "search_naver_news",
  "네이버 뉴스 검색 API로 최신 뉴스를 검색합니다.",
  {
    query: z.string().describe("검색 키워드 (예: '강남구 행사', '더벤티', '오늘 날씨')"),
    display: z.number().int().min(1).max(100).optional().default(10).describe("가져올 뉴스 수 (기본 10, 최대 100)"),
  },
  async ({ query, display = 10 }) => {
    if (!CLIENT_ID || !CLIENT_SECRET) {
      return { content: [{ type: "text", text: "Naver API 인증 정보가 설정되지 않았습니다." }] };
    }

    const url = new URL("https://openapi.naver.com/v1/search/news.json");
    url.searchParams.set("query", query);
    url.searchParams.set("display", String(display));
    url.searchParams.set("sort", "date");

    const res = await fetch(url.toString(), {
      headers: {
        "X-Naver-Client-Id": CLIENT_ID,
        "X-Naver-Client-Secret": CLIENT_SECRET,
      },
    });

    if (!res.ok) {
      return { content: [{ type: "text", text: `Naver API 오류: ${res.status} ${res.statusText}` }] };
    }

    const { items = [] } = await res.json();
    if (items.length === 0) {
      return { content: [{ type: "text", text: "검색 결과가 없습니다." }] };
    }

    const clean = (s) => (s ?? "").replace(/<[^>]*>/g, "").trim();
    const text = items
      .map((item) =>
        `제목: ${clean(item.title)}\n설명: ${clean(item.description)}\n링크: ${item.originallink || item.link}\n발행일: ${item.pubDate}`
      )
      .join("\n\n---\n\n");

    return { content: [{ type: "text", text }] };
  }
);

await server.connect(new StdioServerTransport());
